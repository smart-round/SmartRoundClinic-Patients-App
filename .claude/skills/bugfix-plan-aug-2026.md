# Bug-fix plan — five issues reported 2026-08-03

Investigated 2026-08-07 across all three repos:

- `SmartRoundClinicBackend` (branch `dev`)
- `SmartRoundClinic-doctor` (branch `cr-smrc-0001-referral-doctor-chat`)
- `SmartRoundClinicPatinet` (branch `main`)

Every root cause below was traced to specific code. Line numbers are as of the
commits listed above. Nothing here has been fixed yet — this is the plan only.

| # | Issue | Severity | Root cause confirmed | Repos touched |
|---|---|---|---|---|
| 2 | Paid booking lost when sheet closed | **Critical — patients pay twice** | Yes | patient (+ backend, optional) |
| 3 | Large chat uploads slow / fail silently | High | Yes | patient, doctor |
| 5 | Image shows `filename.png` in chat list | Medium | Yes | backend, patient, doctor |
| 4 | Camera photo shows attachment ID in chat list | Medium | Yes | backend, patient, doctor |
| 1 | Patient avatar missing on incoming call | Low | Yes | backend, doctor |

Suggested order: **2 → 3 → 5/4 → 1**. Issue 2 is losing money. Issues 4 and 5
share one fix. Issue 1 is cosmetic and self-contained.

---

## Issue 2 — Paid booking is lost if the sheet is closed before "Booking Complete"

**Reported:** patient pays, closes the payment bottom sheet before it says
"Booking Complete", and the booking never appears. The slot is still offered as
available and they have to pay a second time.

### Root cause

The appointment is created **client-side, after** the app observes the payment
completing. Closing the sheet destroys the only thing tracking that work.

`shared/.../presentation/main/Services/ServicesViewModel.kt`

`startPolling` (:283) polls IntaSend every 3 s and only on `"COMPLETE"` calls
`confirmBookingAfterPayment` (:299), which is the *first* point that persists
anything durable:

```kotlin
// :322
private fun confirmBookingAfterPayment(transactionRef: String?, invoiceId: String?) {
    ...
    persistPendingBookingPayment(doctorId, date, slot, ref, invoiceId, ...)  // :328
    attemptBooking(...)                                                       // :329
}
```

`dismissStkPush` (:479) — called when the sheet is closed — tears all of it down:

```kotlin
fun dismissStkPush() {
    pollJob?.cancel()        // kills the poller mid-flight
    stkPushData = null
    stkError = null
    stkPollState = null
    pendingDoctorId = null   // wipes the booking context
    pendingDate = null
    pendingSlot = null
}
```

**The losing window:** M-Pesa has debited and the IntaSend invoice is `COMPLETE`,
but the app has not yet polled it (up to 3 s, plus a further 2 s deliberate wait
at :298 for the webhook). Close the sheet in that window and:

1. `pollJob` is cancelled, so `"COMPLETE"` is never observed.
2. `persistPendingBookingPayment` therefore never runs — `PENDING_BOOKING_PAYMENT_KEY`
   is never written.
3. `resumePendingBookingPayment` (:357) reads that key on next launch, finds
   nothing, and has nothing to recover.
4. Money is gone, no appointment exists, and the slot stays bookable.

The recovery machinery is already built and correct — it is simply armed too
late. The same hole exists if the ViewModel is cleared (navigating away) or the
poll times out after ~60 s (`repeat(20)` × 3 s at :286).

Backend confirms there is no server-side safety net: `HandleIntaSendWebhookUseCase`
only updates payment status and logs; it never creates an appointment.

### Fix

**A. Persist the intent at initiation, not at completion** (primary fix, patient app)

Move `persistPendingBookingPayment` from `confirmBookingAfterPayment` up into the
`Resource.Success` branch of `initiateStkPush` (:254–270), as soon as
`invoiceId`/`transactionRef` are known. Then a paid-but-unbooked payment is always
recoverable by the existing `resumePendingBookingPayment` path.

Requires `PendingBookingPayment.transactionRef` to tolerate being written before
payment confirmation — check whether the STK initiation response already returns
it (it does: `data.transactionRef` at :266).

**B. Stop `dismissStkPush` from destroying recovery state**

It must no longer null `pendingDoctorId`/`pendingDate`/`pendingSlot` while a
payment is in flight, and should not cancel `pollJob` outright. Options:

- Let the poll continue in the background after dismissal and book silently
  (best UX — booking completes even though the sheet is gone), or
- Leave the persisted record and let `resumePendingBookingPayment` catch it on
  next app open (simpler, but the patient sees nothing until relaunch).

Recommend the first, with the second as backstop.

**C. Make `resumePendingBookingPayment` run more often**

Confirm where it is currently invoked. It should fire on every app foreground and
on entering Bookings, not only at cold start.

**D. Server-side reconciliation** (backend — strongly recommended, larger)

Client-side booking cannot be made fully safe: the app can be force-killed between
debit and booking. `InitiateStkPushPreBookingUseCase` already stores `doctorId`
and `patientId` on `PaymentEntity` (:129) but sets `appointmentId = null` (:127)
and stores no date/slot.

Plan: add `intendedDate` / `intendedSlotStart` to `PaymentEntity` at pre-booking
initiation, then have `HandleIntaSendWebhookUseCase.handleComplete` create the
appointment when a `COMPLETE` payment has a booking intent and no `appointmentId`.
The existing client `attemptBooking` already tolerates this — it treats a spent
`transactionRef` ("already been used") as terminal, so a booking made by the
webhook first will not double-book.

Also worth adding: a reconciliation job for `COMPLETE` payments with
`appointmentId == null` older than N minutes, to catch webhook failures.

### Verification

- Pay, then close the sheet at ~1 s, ~4 s, ~10 s after the M-Pesa PIN prompt.
  All three must produce exactly one appointment and consume the slot.
- Pay, then force-kill the app before booking; reopen; the booking must appear.
- Confirm no path can produce two appointments for one `transactionRef`.

---

## Issue 3 — Large file uploads are extremely slow and fail with no error

**Reported:** slow, sometimes fails silently, file simply never appears in the
chat. Requested behaviour: show "Unable to send file as it is too large. Please
try again" beneath the file.

### Root cause — three compounding problems

**(a) Every request body is fully logged.** `shared/.../core/network/HttpClientFactory.kt`:

```kotlin
install(Logging) {
    logger = ...Napier.d(...)
    level = LogLevel.ALL     // logs headers AND full body
}
```

`LogLevel.ALL` makes Ktor buffer and stringify the entire multipart body. For a
multi-MB image this means the payload is held in memory an extra time and
rendered to a log string on every send. This alone can account for "extremely
slow", and it is active in release builds. **This is the single highest-value fix.**

**(b) A 30-second ceiling on all requests, uploads included.**

```kotlin
install(HttpTimeout) {
    requestTimeoutMillis = 30_000L
    socketTimeoutMillis  = 30_000L
}
```

A large file on mobile data will not finish inside 30 s, producing
`HttpRequestTimeoutException`. Uploads are not retried (`HttpRequestRetry` is
GET-only), which is correct, but the ceiling is far too low for uploads.

**(c) There is no size limit anywhere.** No client-side check in
`ConsultationViewModel.sendFile` (:490) or `ConsultationScreen` (:220–236), and
a backend grep found no multipart size cap. So oversized files are attempted,
not rejected — there is no "too large" error to show because nothing produces one.

**Why it looks silent:** `sendFile` does surface errors — `Resource.Error` calls
`snackbarController.show(...)` and `markFailed(pending)` (:517–518), and
`markFailed` (:563) flips the pending bubble to `failed = true`. So a failure
*should* be visible. Two candidates for the reported silence, to confirm while
fixing:

- The whole `viewModelScope` job dies (screen left / VM cleared) before the
  timeout fires, so neither branch runs and the pending bubble vanishes with the
  screen.
- `defaultRequest { contentType(ContentType.Application.Json) }` sets a JSON
  Content-Type on *every* request, including the multipart upload. If that header
  reaches the server ahead of the multipart boundary type, the server may fail to
  parse the body and return a 2xx-with-no-data or a 4xx that maps to a blank
  message. **Verify this against the real server before assuming.**

### Fix

1. **Drop body logging.** `LogLevel.HEADERS` (or `LogLevel.INFO`), and ideally
   gate logging to debug builds entirely. Apply in both patient and doctor apps.
2. **Per-request timeouts for uploads.** Keep the 30 s global default, but attach
   a longer `timeout { requestTimeoutMillis = ...; socketTimeoutMillis = ... }`
   block to the upload call in `ConsultationRepositoryImpl.uploadFile` (:81) —
   e.g. 5 minutes request, 60 s socket.
3. **Add an explicit size cap.** Define a shared `MAX_CHAT_FILE_BYTES` in
   `common/Constants.kt`. Reject in `sendFile` *before* creating the pending
   bubble, or mark the bubble failed with the exact copy requested:
   *"Unable to send file as it is too large. Please try again"*, rendered beneath
   the file in the bubble (`PendingFile` already carries a `failed` flag — extend
   it with an optional `errorText`). Pick the cap to match whatever the server and
   any reverse proxy actually accept.
4. **Enforce the same cap server-side** in the chat upload route, returning a
   clear message the client can display verbatim.
5. **Verify the multipart Content-Type** actually sent, and stop `defaultRequest`
   from overriding it if it does.
6. Consider showing upload progress for large files, since even a fast upload of
   a big file looks frozen today.

### Verification

Send 1 MB / 10 MB / 50 MB files on wifi and throttled mobile data. Small files
must stay fast; oversized files must fail *immediately* with the specified copy;
nothing may fail silently.

---

## Issues 4 and 5 — Chat list shows attachment ID / filename instead of "Photo"

These are one bug with one fix. Requested behaviour: a camera icon and the copy
**Photo**.

### Root cause

`consultation/.../domain/usecase/chat/ListConversationThreadsUseCase.kt:67`:

```kotlin
private fun previewOf(message: ConsultationMessageEntity): String =
    when (message.messageType) {
        MessageType.TEXT -> message.message ?: ""
        MessageType.FILE -> message.files.firstOrNull()?.fileName ?: "Attachment"
        MessageType.PRESCRIPTION -> "Prescription"
    }
```

The raw `fileName` is used as the thread preview, so:

- **Issue 5** — a gallery upload keeps its real name and shows `xray.png`.
  (The existing test `ListConversationThreadsUseCaseTest.kt:151` actually asserts
  `"xray.png"` — that assertion must be updated as part of the fix.)
- **Issue 4** — a camera capture has no meaningful name. In
  `ConsultationScreen.kt:225–230` the camera launcher keeps whatever FileKit
  produces as long as it contains a dot:
  ```kotlin
  val name = it.name.takeIf { n -> n.isNotBlank() && n.contains('.') } ?: "photo.jpg"
  ```
  FileKit writes camera captures to a temp file named with a generated UUID, which
  *does* contain a dot, so the UUID survives and surfaces as the "attachment ID"
  in the thread list.

`ConsultationFile` already carries `contentType` and `sizeBytes`
(`ConsultationMessageEntity.kt:44–49`), so images are distinguishable — the
preview logic simply ignores that.

### Fix

**Backend** — teach `previewOf` about images, and give clients something better
than a magic string to switch on:

```kotlin
MessageType.FILE -> {
    val f = message.files.firstOrNull()
    when {
        f == null -> "Attachment"
        f.contentType.startsWith("image/") -> "Photo"
        else -> f.fileName
    }
}
```

Add a `lastMessageKind` enum field (`TEXT` / `PHOTO` / `FILE` / `PRESCRIPTION`) to
`ConversationThreadRes` and `DoctorChatThreadRes` so clients choose the icon from
a typed field rather than string-matching `"Photo"`. Mirror the same change in
`doctor-chat`'s `GetMyDoctorChatThreadsUseCase.kt:31`. Update the existing test.

**Clients** — thread the new field through DTO → domain → UI and render a camera
icon beside the preview when the kind is `PHOTO`:

- patient: `ConsultationRes.kt`, `ConversationThread.kt`, chat list screen
- doctor: `ConsultationRes.kt` / `DoctorChatRes.kt`, `ConversationThread.kt` /
  `DoctorChatThread.kt`, `ChatListScreen.kt:253`, `DoctorChatsListScreen.kt:160`,
  `HomeScreen.kt:333`

**Also worth fixing (secondary):** normalise the camera filename at capture to
something human (`photo-<timestamp>.jpg`) in both apps' camera launchers. The UUID
name is also shown inside the message bubble itself
(`ConsultationScreen.kt:681/736/1055/1138`), which the backend fix does not
address.

### Verification

Send a camera photo, a gallery image, a PDF and a text message. The list must
show camera icon + "Photo" for the first two, the filename for the PDF, and the
text for the message — on both apps, and on the doctor home screen.

---

## Issue 1 — Patient profile picture missing on the doctor's incoming-call screen

### Root cause

The picture is never sent. It is absent at every layer:

1. **Backend** — `consultation/.../usecase/call/InviteToCallUseCase.kt:77` resolves
   only the name:
   ```kotlin
   val callerName = messages.getUserName(callerId)
   ```
   and puts just `callerName` into both the WebSocket event (:109) and the FCM
   data payload (:125). `ConsultationWsEventRes.kt:31` has `callerName` and no
   picture field. Same in `doctor-chat`'s `InviteToDoctorCallUseCase.kt:49/64/73`.
2. **Doctor app** — `domain/model/CallInvite.kt`: `IncomingCall` and
   `IncomingDoctorCall` carry `callerName` but no picture.
3. **UI** — `IncomingCallActivity.kt` only ever reads `EXTRA_CALLER_NAME` (:87,
   :199) and renders the name (:245).

A `getUserInfo(userId)` helper returning *(name, picture)* already exists and is
used by `ListConversationThreadsUseCase.kt:46` — so the data is one call away.

### Fix

1. **Backend:** swap `getUserName` for `getUserInfo` in both invite use cases; add
   `callerPicture: String?` to `ConsultationWsEventRes` / `DoctorChatWsEventRes`
   and to the FCM data map. Additive and backward-compatible.
2. **Doctor app:** add `callerPicture: String?` to `IncomingCall` /
   `IncomingDoctorCall`; parse it in the WS handler and the push handler
   (`IncomingCallHandler`, `CallKitBridge`); pass it as a new intent extra
   (`EXTRA_CALLER_PICTURE`) through `IncomingCallActivity` /
   `IncomingDoctorCallActivity`; load with `AsyncImage` and keep the current
   placeholder as the fallback.
3. **iOS:** CallKit shows only a name string; the picture will apply to the in-app
   screen, not the system call UI.
4. Check whether the patient app has the mirror bug for doctor-initiated calls.

### Verification

Call a doctor from a patient account with a profile picture — the avatar must
appear on the incoming-call screen from both a cold-start push and an in-app
WebSocket ring. A patient with no picture must still show the placeholder.

---

## Cross-cutting notes

- **Issues 1, 4 and 5 all need a backend deploy** before the client changes do
  anything visible. Sequence deploys accordingly.
- All backend changes here are **additive** (new nullable/defaulted response
  fields), so older clients keep working.
- Issue 2 fix **A + B is patient-app-only** and can ship immediately without
  waiting on the backend; fix D is the durable one and should follow.
- The `LogLevel.ALL` finding in issue 3 affects **every** request in both apps,
  not just uploads — it is worth fixing on its own merits, and it may also be
  leaking request bodies (including auth payloads) into device logs. Treat that
  as a security item, not only a performance one.
