# Medical record changes → patient chat, with an "Edited" marker

**Scope:** backend (`SmartRoundClinicBackend`) + doctor app + patient app, all three in one round.
**Date:** 2026-08-10

## Direction given by the user

1. When a doctor changes or edits a medical record, the change is sent to the patient **as a chat message**, with an **"Edited" status at the bottom** of the card.
2. **Append-only**: post a *new* card marked "Edited" and leave the previous card in place. The thread stays a history; the patient sees the change arrive.
3. Post when a **notifiable (clinical) field** changes — chosen from four options after review
   (option **A**, field-level diff). A first save always posts. On a revision, the card is posted
   only if `diagnosis`, `prescription`, `labRequests` or `referralNote` differ from what was stored;
   editing only `summary` / `additionalNotes` stays silent. Because the diff knows the field names,
   the marker reads "Edited — prescription, lab requests" rather than a bare "Edited".

---

## Current behaviour (verified against the code)

There is no separate edit endpoint. `POST /medical-records` is the only write
(`medical-records/.../presentation/controller/MedicalRecordController.kt:26`) and runs
`SaveMedicalRecordUseCase`, which calls `repository.upsert()`. The upsert is keyed on
`appointmentId`: if a record exists for that appointment **and** belongs to the same doctor, the
document is replaced, keeping the original `id` and `createdAt` and stamping `updatedAt`
(`data/repository/MedicalRecordRepositoryImpl.kt:19-37`). Create and edit are the same call.

Every save then does three things (`domain/usecase/SaveMedicalRecordUseCase.kt`):

| Step | Line | Behaviour | Problem |
|---|---|---|---|
| Push notification | :29-40 | `MEDICAL_RECORD_UPDATED`, "Your medical record has been updated by your doctor", deep-links to Medical History | Says "updated" even on first creation |
| Chat message | :41-56 | Saves a **new** `PRESCRIPTION` message with the record JSON — **only if `prescription.isNotEmpty()`** | Lab-request-only / notes-only / diagnosis-only saves post **nothing** |
| Broadcast | — | Mongo change stream (`ConsultationMessageRepositoryImpl.kt:82-92`, filtered `operationType == "insert"` + doctorId/patientId) pushes the insert to a connected socket | No offline push on this path — `notifyOfflineParticipant` is only called from the WS `TEXT` branch (`ConsultationChatService.kt:90`) |

Three consequences, which together are the reported bug:

1. **Lab requests never reach the chat.** Gated out by `prescription.isNotEmpty()`.
2. **Edits are indistinguishable duplicates.** An edit posts a second, complete card with a fresh
   timestamp and nothing marking it as a revision. Observed on device: two identical
   "Prescription / Diagnosis: bxbdb / panadol — 500 mg" bubbles a minute apart.
3. **The card payload lies about itself.** The use case serialises `model.toRes()` — the *request*
   model from `SaveMedicalRecordReq.toModel()`, which hardcodes `updatedAt = null`, a fresh
   `ObjectId()` and a fresh `createdAt` (`presentation/dto/request/MedicalRecordReq.kt:30-43`) —
   instead of the persisted entity returned by the upsert. So every card claims to be a brand-new
   record with a `id`/`createdAt` that match nothing in the database.

Card rendering, both apps: only **diagnosis, prescription items, summary** are drawn
(patient `ConsultationScreen.kt:1016-1058`, doctor `ConversationScreen.kt:1068-1087`).
`labRequests`, `referralNote` and `additionalNotes` are decoded into `MedicalRecordData` and then
dropped on the floor.

---

## Target behaviour

Any successful `POST /medical-records` posts one `PRESCRIPTION`-type card into the patient↔doctor
thread. If the save replaced an existing record, the card renders **"Edited"** as its last line,
under the timestamp. Previous cards are untouched.

The card shows every field the record actually carries: diagnosis, prescription items, lab requests,
referral note, summary, additional notes.

---

## Key design decision: no schema or DTO change needed

`upsert` already returns the **persisted** entity, and that entity already distinguishes the two
cases: `updatedAt` is stamped on replace and stays `null` on insert. `MedicalRecordRes.updatedAt`
exists and `toRes()` maps it, and **both apps' `MedicalRecordData` already parse `updatedAt` and
`labRequests`** (patient `MedicalRecordRes.kt:17-31`, doctor `MedicalRecordRes.kt:27,30`).

So fixing consequence #3 above — serialise the upsert *result* instead of the request model —
delivers the "Edited" signal for free: the apps derive it as `record.updatedAt != null`.

No new field on `ConsultationMessageEntity`, no `ConsultationMessageRes` change, no change to
either app's `ConsultationMessage` domain model, no Mongo migration. Rejected alternative: adding
`isRecordEdit: Boolean` to the message entity — more moving parts, needs the BSON codec to tolerate
the field being absent on every existing document, and duplicates state already in the payload.

---

## Backend changes — as implemented

0. **New `domain/model/MedicalRecordChange.kt`**: `enum MedicalRecordField { DIAGNOSIS, PRESCRIPTION,
   LAB_REQUESTS, REFERRAL_NOTE }` (the notifiable set — prose is deliberately absent) and
   `MedicalRecordSaveResult(record, isUpdate, changedFields)`.
   `MedicalRecordRepository.upsert` now returns `Resource<MedicalRecordSaveResult?>`; the impl diffs
   the previous document it already loaded for the ownership check, so the diff costs no extra read.

1. **Replace the prescription gate with the clinical diff.** Post when `!isUpdate ||
   changedFields.isNotEmpty()`. Merged into the single `Resource.Success` block so there is one
   success path, not two.
2. **Serialise the persisted record.** Use the entity from `result.data` —
   `result.data?.toModel()?.toRes()` — rather than `model.toRes()`. This fixes the card's `id`,
   `createdAt` and `updatedAt`, and is what makes "Edited" derivable client-side.
   Guard: if `result.data` is somehow null, skip the message rather than posting a bogus card.
3. **Notify an offline patient.** Inject `NotifyOfflineConsultationParticipantUseCase` (already in
   the `:consultation` module, which `:medical-records` already depends on for
   `ConsultationMessageRepository`) and call it after the message is saved, so the card behaves like
   any other chat message when the patient isn't connected. Preview text: `"Prescription"` when the
   record has drugs, else `"Medical record updated"`. Register in
   `medical-records/.../koin/MedicalRecordsKoinModule.kt`.
4. **Collapse the double push.** With (3) in place a single save would fire two notifications. Send
   only the chat one — the card now always lands in the thread, and `NEW_CHAT_MESSAGE` deep-links
   there. Remove the `MEDICAL_RECORD_UPDATED` send. **Flagged**: this drops the only producer of
   that event, so the patient app's `NotificationEvent.ToMedicalHistory` deep link goes dormant
   (the handler stays, harmless). See assumptions.
5. Keep the `runCatching` wrappers: a notification or chat-message failure must never fail the save.

Not changed: `MedicalRecordRepository.upsert`'s signature, the controller, the request DTO, the
change-stream filter (the new message is still an `insert`, so it streams as-is).

## Doctor app + patient app changes

Symmetrical, one file each — patient
`presentation/main/chat/ui/ConsultationScreen.kt` (`PrescriptionCard`, ~:970-1069) and doctor
`presentation/main/chat/ui/ConversationScreen.kt` (`PrescriptionCard`, ~:1045-1090):

1. Render the dropped fields, each only when non-empty, in record order: diagnosis → prescription
   items → **lab requests** → **referral note** → summary → **additional notes**. Lab requests as a
   bulleted list styled like the prescription items; the two notes as body text like `summary`.
2. Add the **"Edited"** line as the card's last element, below the existing timestamp, shown when
   `record.updatedAt != null`. Same muted style as the timestamp. A private `editedLabel()` maps
   `editedFields` to prose — "Edited — prescription, lab requests" — falling back to a bare "Edited"
   when the list is empty, which is what pre-existing cards decode to.
3. Because a record with no drugs now produces a card, the "Rx" chip + "Prescription" heading needs
   a neutral variant — title becomes "Medical Record" when `record.prescription.isEmpty()`.

`ListConversationThreadsUseCase.kt:78` already previews these as "Prescription" in the thread list;
with drug-less records now posting, that preview becomes "Medical record" under the same condition
as the card title.

---

## Edge cases

- **Concurrent edits by the same doctor** — last write wins, as today; each save posts its own card,
  so the thread shows the sequence. No locking introduced.
- **A different doctor tries to edit** — already rejected by `upsert` ("You are not authorized to
  edit this medical record"); no card posts because the result is `Resource.Error`.
- **Empty save** (doctor opens the form and saves with nothing filled) — a *first* empty save still
  posts a title-only card; a re-save of an already-empty record changes no notifiable field, so it
  stays silent. The doctor app's own form gating is where an empty-save guard belongs, not the
  notification path.
- **Prose-only edit** — no card, no notification. The new text still reaches the patient on the next
  card that does post, since every card carries the whole record.
- **No behavioural change to Medical History** — the record itself is unchanged; this only affects
  what the chat thread shows.
- **Old cards keep working.** Existing messages carry `updatedAt = null` in their payload, so they
  render exactly as they do today — no "Edited" line, no back-fill.

## Assumptions to audit

| # | Assumption | Rationale | Cost if wrong |
|---|---|---|---|
| 1 | "Edited" derives from `record.updatedAt != null` rather than an explicit message flag | Already persisted, already parsed by both apps, zero migration | If a future feature stamps `updatedAt` for some other reason, a card could read "Edited" spuriously |
| 2 | Replacing the `MEDICAL_RECORD_UPDATED` push with the chat push (step 4) | One action should raise one notification, and the chat deep link now leads to the full card | The Medical History deep link loses its trigger; reinstate the send if you still want that entry point |
| 3 | Drug-less records reuse the `PRESCRIPTION` message type, re-titled "Medical Record" | Avoids a new `MessageType` (which both apps and the thread-preview mapper would have to learn) and keeps old messages valid | If medical-record cards later need to be filtered separately from prescriptions, the type is shared |
| 4 | Lab requests render as plain strings | `labRequests: List<String>` — no structure to show | None; matches the model |

## Execution notes

- The plan file is this document, per the user's convention of keeping plans under
  `.claude/skills/` for later audit.
- **Branching**: both app repos currently have uncommitted work in their trees (the patient repo has
  this session's Articles/chat design changes; the doctor repo is mid-`cr-smrc-0001-referral-doctor-chat`
  with a large dirty tree). Creating feature branches would carry that unrelated work along, so
  branch creation is left to the user rather than done unilaterally.
- Order: backend first (it defines the payload), then the two card renderers, compiling each repo as
  it goes.
