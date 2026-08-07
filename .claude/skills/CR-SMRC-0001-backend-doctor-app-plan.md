# CR-SMRC-0001 — Doctor Referral + Doctor-to-Doctor Chat
## Round 1 scope: Backend + Doctor App only (patient app deferred to a follow-up round)

## Context

Smart Round Clinic's platform today only supports direct, independent patient↔doctor appointments (backend `SmartRoundClinicBackend`, doctor app `SmartRoundClinic-doctor`, patient app `SmartRoundClinicPatinet`). CR-SMRC-0001 (`.claude/skills/SMRC-CR-0001-...pdf`) adds a doctor-to-doctor referral workflow and doctor-to-doctor chat. This round covers **backend + doctor app only**; the patient app follows in a separate round to keep context focused.

This plan supersedes the CR's literal wording where the user gave more specific direction in review. Two deliberate deviations from the CR-as-written, confirmed by the user:

1. **Referrals get an explicit Decline**, not just Pending→Accepted — so a referral reaches a clean terminal state instead of sitting pending forever.
2. **Doctor-to-doctor chat gets full call parity** (audio/video, via a dedicated Cloudflare RealtimeKit room) — the CR's §3.2 explicitly listed doctor-doctor calling as out of scope ("text chat and document attachments only"); the user overrode this explicitly and named the RealtimeKit room concept ("doctors_lounge"). Flagging this clearly since it's a real scope expansion beyond the signed CR, for the user's own audit trail.

**Confirmed architecture decision**: doctor-to-doctor chat (including calling) is built as a **new, parallel backend module and new app screens** — not a generalization of the existing patient↔doctor `:consultation` system. That system hardcodes doctor/patient pairing at the type level (`ConsultationChatController.resolvePair` branches on `role=="DOCTOR"`, which is structurally meaningless when both sides are doctors) and is under active, delicate development (recent commits are all call-quality/CallKit fixes; there's a dedicated `rtk-core-sdk-call-screen` worktree in the doctor app right now). A parallel module reuses the same **infra primitives** (Cloudflare RealtimeKit client, R2 file storage, MongoDB change-stream broadcast, WebSocket registry, APNs VoIP push) without touching the existing domain code, so there is zero regression risk to the working patient-doctor chat/call feature.

**Execution hygiene**: do this work on a dedicated feature branch in both repos (e.g. `feature/cr-smrc-0001-referral-doctor-chat`), branched from each repo's current `main`, per the user's request — this is the first step of execution, before any code changes.

---

## Flow 1 — Doctor Bookings screen restructure

### Current state (confirmed in `BookingListScreen.kt` / `BookingsRoot.kt`)
Two flat tabs: **Upcoming** and **Past**, rendered by a hand-rolled pill `BookingTabRow` (not Material `TabRow`). `BookingsRoot.kt` holds `selectedTab: BookingTab` as flat state and passes the single `viewModel.appointments` list (Room-cached, `observeAppointments()`) into `BookingListScreen`, which filters client-side by date/status.

### Target state
Two **top-level** tabs: **Consultation** (primary/default) and **Referral**.
- **Consultation** tab contains the existing Upcoming/Past **as sub-tabs**, unchanged in behavior — same filter logic, same `BookingCard`, same detail flow. This is a pure UI nesting change, no data/filter-logic change.
- **Referral** tab (sibling, not nested) shows appointments where **this doctor is the receiving doctor of an accepted referral** — i.e., real booked appointments (any status) that were created via someone else's referral. Same `BookingCard` design, with one addition: a small referring-doctor avatar + **"Referred by: Dr. {name}"** caption, inserted between the date/time row and the patient-name row of the existing card. Tapping a referral card opens the exact same `AppointmentDetailScreen` flow as any consultation appointment — no special detail screen, per explicit instruction ("nothing changes").

### Implementation
- `BookingListScreen.kt`: introduce `internal enum class BookingTopTab { CONSULTATION, REFERRAL }`. Keep `internal enum class BookingTab { UPCOMING, PAST }` exactly as-is for the nested sub-tabs. Add a `BookingTopTabRow` above the existing `BookingTabRow`, shown always; `BookingTabRow` (Upcoming/Past) only rendered when `CONSULTATION` is selected.
- New `ReferralListScreen` content (same file or a sibling composable): filters the same `appointments: List<Appointment>` on `appointment.referralId != null`, no date/status filtering (shows all statuses — booked/confirmed/completed/cancelled — newest first), reusing `BookingCard` with an added optional `referredBy: ReferredByInfo?` param that renders the avatar+caption when present.
- `BookingsRoot.kt`: `selectedTab` state becomes two pieces — `selectedTopTab: BookingTopTab` (retained) plus the existing `selectedTab: BookingTab` (retained, only meaningful under Consultation). No new ViewModel or network call needed — same `viewModel.appointments` StateFlow drives both tabs.
- **Refer-out CTA (carried over from the CR, unchanged in placement)**: this restructuring is about *viewing* referred-in appointments; the doctor's ability to *refer a patient out* still lives where the CR originally specified — on `AppointmentDetailScreen` for a `COMPLETED` appointment (reached via Consultation → Past), gated on rating + medical summary + prescription already submitted (all three already loaded together in `BookingsRoot.kt` via `BookingsViewModel`/`MedicalRecordViewModel`/`RatingViewModel`). New screens: `ReferralReasonScreen.kt` (free-text reason) → doctor picker (ported Services UI, Flow 3) → confirm. New destinations in `bookings/destinations/Screens.kt`: `ReferralReason(appointmentId)`, `ReferralConfirm(appointmentId, reason, receivingDoctorId)`.

### Domain/data layer additions (doctor app)
- `domain/model/Appointment.kt` / `AppointmentStatus`: add `referralId: String?`, `referredByDoctorName: String?`, `referredByDoctorPicture: String?` (nullable, populated only when tagged), threaded through DTO → Room entity → domain mapper, same pattern as every other field in this file.
- New `domain/model/Referral.kt`, `domain/repository/ReferralRepository.kt`, `data/repository/ReferralRepositoryImpl.kt`, response DTOs, `domain/usecase/referral/{CreateReferralUseCase, GetReferralEligibilityUseCase}.kt`, registered in `koin/RepositoryModule.kt`/`koin/UseCaseModule.kt` following the existing flat-list convention.

### Edge cases handled
- **Referral card data source**: referring-doctor name/photo is **denormalized onto the `Referral` record at creation time** (snapshot), not live-joined from the doctor's current profile on every appointment-list load — cheaper (the scheduling endpoint does one lookup-by-id instead of a join across collections) and immune to the referring doctor later changing their name/photo or being deactivated.
- **Re-referring after a decline**: a doctor can create a new referral for the same source appointment after a prior one was Declined. Only one *active* (Pending, or Accepted-but-not-yet-booked) referral is allowed per source appointment at a time — `CreateReferralUseCase` checks this and returns a clear error ("This appointment already has an active referral") otherwise.
- **Self-referral / unverified target**: blocked (`receivingDoctorId != referringDoctorId`; both parties must pass `VerifiedDoctorResolver`, §Backend).
- **Referral chaining**: a referred-in appointment can itself become the source of a further referral once completed — nothing in the CR forbids multi-hop referrals, and blocking it would need an explicit rule the CR doesn't state, so it's allowed by default. Flagging as an assumption to audit.
- **Cancelled/no-show referred appointments**: still shown in the Referral tab (existing status badge already renders Cancelled/No-Show) — the tag is orthogonal to status.

---

## Flow 2 — Wallet moves from bottom nav into Profile

### Current state (confirmed in `MainRoot.kt` / `HomeRoot.kt`)
`Wallet` is a top-level bottom-nav tab (`BottomTab(Wallet, "Wallet", ...)` at line 112, `entry<Wallet> { WalletRoot(...) }` at line 244 in `MainRoot.kt`). Profile itself is not a separate bottom tab — it's nested inside the **Home** tab's own backstack (`HomeRoot.kt` owns `entry<ProfileList>`, `entry<BankingDetails>`, etc., pushed via `backStack.add(ProfileList)`).

### Target state
Remove `Wallet` from the bottom nav entirely. Add a **"Wallet"** menu item to the Profile list, positioned immediately **below "Banking Details"** (confirmed at `ProfileListScreen.kt` lines 152-155, right before "Licences"). Tapping it opens the existing `WalletRoot` screen unchanged — this is a pure navigation-entry-point relocation, zero changes to wallet functionality itself.

### Implementation
- `MainRoot.kt`: remove the `BottomTab(Wallet, ...)` line (112) and the top-level `entry<Wallet> { WalletRoot(...) }` block (244).
- `HomeRoot.kt`: add `entry<Wallet> { WalletRoot(...) }` inside its own `NavDisplay` (alongside the existing `entry<ProfileList>`/`entry<BankingDetails>`), reusing the same `Wallet` NavKey object (`presentation.main.destinations.Wallet`) already imported elsewhere — no new NavKey type needed.
- `ProfileListScreen.kt`: add `onWallet: () -> Unit = {}` param, insert a new `ProfileMenuItem(label = "Wallet", onClick = onWallet)` between the "Banking Details" item (line ~152-155) and "Licences" (line ~158).
- `HomeRoot.kt`'s `entry<ProfileList>` wiring: thread `onWallet = { backStack.add(Wallet) }` (mirrors the existing `onBankingDetails = { backStack.add(BankingDetails) }` at line 117).

### Edge cases handled
- **Notification deep-links**: confirmed via grep — no existing push-notification deep-link currently targets the Wallet tab directly, so no deep-link rewiring is needed. (If a future payout notification is added, it should route `selectTab(Home)` + push `[ProfileList, Wallet]` onto Home's backstack, the same pattern already used for `pendingHomeDestinations = listOf(ProfileList, LicenceManagement)` at MainRoot.kt line 281.)
- **Bottom nav reflow**: removing one tab shifts icon spacing — purely cosmetic, `BottomNavBar`'s `Row` with `weight(1f)` items already handles variable tab counts (5→4 today, going to 4 tabs post-removal, 5 again once Services is added in Flow 3 — net tab count stays the same as today).

---

## Flow 3 — New "Services" tab in bottom nav (between Home and Bookings)

### Current state
This entire browsing pattern **does not exist in the doctor app today**. It exists only in the patient app (`SmartRoundClinicPatinet/shared/.../presentation/main/Services/`): `ServicesRoot.kt`, `ServiceCategoriesScreen.kt` (search bar + specialty card grid), `DoctorsByCategoryScreen.kt` (doctor list for a specialty, `DoctorListCard` with a "Book Now" pill CTA + separate card-tap → profile), `DoctorProfileScreen.kt` (About / **Specialty & Pricing** / Articles tabs, "Book Now" CTA at the bottom).

### Target state
Bottom nav order becomes: **Home, Services, Bookings, Articles, Chat** (Wallet removed per Flow 2). The Services tab is a doctor-facing port of the patient pattern:
- Landing screen: search bar (search specialities) + specialty card grid — same visual design as the patient app.
- Tapping a specialty → doctor list for that specialty, same list design, **except**: the "Book Now" pill CTA is replaced with a **chat icon** button. Tapping the chat icon **immediately initiates a chat** with that doctor (bypasses the profile screen — same "quick action" precedent as the patient app's Book Now, which is also a direct action from the list). Tapping anywhere else on the card opens that doctor's profile — unchanged interaction split from the patient app.
- Doctor profile screen: same as the patient app's version, with exactly two differences: (1) the **"Specialty & Pricing" tab is removed entirely** (this ported screen is only ever reached by a doctor caller, so it's deleted outright rather than conditionally hidden — simpler, no dead conditional branch); (2) the bottom "Book Now" button becomes **"Chat"**, wired to the same connect-and-navigate action as the list's chat icon.
- "Connect" action (from either entry point): calls `InitiateDoctorChatUseCase` → backend creates/finds the doctor-doctor thread → navigate to the new doctor conversation screen (Flow 4) under the Chat tab's "Other Doctors" sub-tab.

### Implementation
- Port into a new package `composeApp/src/commonMain/kotlin/ke/co/smartroundclinic/doctor/presentation/main/services/`: `ServicesRoot.kt`, `ServiceCategoriesScreen.kt`, `DoctorsByCategoryScreen.kt`, `DoctorProfileScreen.kt`, `ServicesViewModel.kt`, adapted per the differences above.
- New doctor-app domain layer (doesn't exist yet — this app only has `SpecialityRepository`/`Speciality.kt` for the doctor's *own* specialization picker, reusable for the category list): `domain/model/Doctor.kt` (browsable-doctor shape, mirrors patient app's), `domain/repository/DoctorDirectoryRepository.kt`, `data/repository/DoctorDirectoryRepositoryImpl.kt` calling the backend's existing `GET /doctor/recommendations?specializationId=&excludeDoctorId=<self>&page=&size=` (new `excludeDoctorId` param added backend-side, §Backend), `domain/usecase/directory/GetRecommendedDoctorsUseCase.kt`.
- `MainRoot.kt`: add `BottomTab(Services, "Services", ...)` between the `Home` and `Bookings` entries (line ~108-109), new `Services` NavKey in `presentation/main/destinations/Screens.kt`, new `entry<Services> { ServicesRoot(...) }` block mirroring the existing tab-entry pattern.
- Koin: register the new repo/use cases in `koin/RepositoryModule.kt`/`koin/UseCaseModule.kt`.

### Edge cases handled
- **Doctor excludes self from search results**: backend `excludeDoctorId` param (new, additive to the existing recommendations endpoint) — a doctor never sees themselves in their own specialty's doctor list.
- **Unverified/non-monetized doctors never appear**: `GET /doctor/recommendations` already filters to verified doctors today (confirmed in `RecommendationRepositoryImpl`) — no change needed there, this constraint is inherited for free.
- **Chat icon tapped twice quickly / thread already exists**: `InitiateDoctorChatUseCase` is idempotent — find-or-create by the sorted doctor-pair key, so repeated taps resolve to the same thread rather than creating duplicates.
- **Empty specialty (no doctors)**: reuse the same empty-state pattern already present in the patient app's `DoctorsByCategoryScreen.kt`.

---

## Flow 4 — Chat tab split: Consultations / Other Doctors (with full call parity)

### Current state (confirmed in `ChatRoot.kt` / `ChatListScreen.kt` / `ConsultationViewModel.kt`)
Single screen, header hardcoded `DashboardHeader(title = "Consultations", ...)`, listing only patient threads. Chat, file-attach, typing/presence, and calling (`CallScreen.kt`, `OutgoingCallScreen.kt`, `RtkCallController.kt` against Cloudflare RealtimeKit) all exist today for patient threads only, backed by the `:consultation` backend module.

### Target state
Chat tab gets two sibling tabs (same hand-rolled tab-row idiom as `BookingListScreen`'s `BookingTabRow` — this codebase's established pattern, not Material `TabRow`):
- **Consultations** — existing patient-chat screen, byte-for-byte unchanged.
- **Other Doctors** — new. Lists the signed-in doctor's doctor-to-doctor threads. Has a search icon in its header that, when tapped, **transitions the header into a search bar** (simple `isSearching` boolean toggle, no precedent in this app to reuse — styled to match the patient app's `OutlinedTextField` search bar in `ServiceCategoriesScreen.kt` for visual consistency) to find any verified doctor on the platform by name and start a chat directly.
- Full feature parity with patient chat: free-text, file/image attachments, typing/presence, **and calling** — same `CallScreen`/`OutgoingCallScreen` **UI components reused as-is** (they're presentational, driven by callbacks/state), backed by a **new, separate** `DoctorConversationViewModel`/`RtkDoctorCallController` wired to the new doctor-chat backend endpoints, keeping zero shared mutable state with the existing patient-call path (full decoupling, per the "no regression" requirement).

### Implementation
- `ChatRoot.kt`: add top-level tab state (`ChatTopTab { CONSULTATIONS, OTHER_DOCTORS }`, retained). `ChatListScreen.kt`'s hardcoded title becomes the tab row; `Consultations` renders the existing screen unchanged; `OTHER_DOCTORS` renders a new `DoctorThreadListScreen.kt`.
- New destinations (parallel to, not reusing, the existing `Conversation`/`Call`/`OutgoingCall` — per the decoupling decision): `DoctorConversation(threadId, otherDoctorName, otherDoctorPicture)`, `DoctorCall(threadId, isVideo)`, `OutgoingDoctorCall(threadId, calleeName, isVideo)` in `chat/destinations/Screens.kt`.
- New `DoctorConversationScreen.kt` (near-duplicate of `ConversationScreen.kt`, same layout/behavior, different backing ViewModel) and reuse of `CallScreen.kt`/`OutgoingCallScreen.kt` composables with a new `RtkDoctorCallController` (near-duplicate of `RtkCallController.kt`, pointed at `/doctor-chat/...` endpoints instead of `/chat/...`).
- New `DoctorChatViewModel.kt`: mirrors the message send/receive/typing/presence slice of `ConsultationViewModel`, connecting to `WS /doctor-chat/threads/{threadId}` instead of `WS /chat/{otherUserId}`.
- Push notifications: `NotificationSetup.kt`'s `onNotificationClicked`/`onPayloadData` switches get new cases for doctor-chat call signaling and new-message events (new event constants, §Backend), routing to the new `DoctorConversation`/`DoctorCall` destinations via a new `NotificationEvent.ToDoctorConversation(threadId, ...)`/`ToDoctorCall(...)` in this app's `core/notification/NotificationDeepLink.kt`.
- Koin: register `DoctorChatRepository`, `DoctorChatViewModel`, `RtkDoctorCallController` in `RepositoryModule.kt`/`UseCaseModule.kt`.

### Edge cases handled
- **Search-while-typing debounce**: mirror whatever debounce/throttle pattern (if any) exists in the patient app's specialty search; otherwise a simple 300ms debounce on the new doctor-name search field, client-side.
- **Starting a chat from Services (Flow 3) vs. from the search bar here**: both funnel through the same `InitiateDoctorChatUseCase`/thread find-or-create, so a doctor reached via either path always lands in the same single thread.
- **Incoming doctor-to-doctor call while the app is backgrounded**: reuses the existing `com.mmk.kmpnotifier`/APNs-VoIP/CallKit plumbing (`IncomingCallHandler`), which is already payload-driven and generic — only needs a new payload event name and, on click/answer, routing to `DoctorCall` instead of `Call`. No changes to the CallKit/foreground-service Android/iOS actuals themselves.
- **Two doctors calling each other simultaneously (glare)**: mirror whatever collision-handling already exists in `JoinThreadCallUseCase`/`InviteToCallUseCase` for the patient-doctor path (`findActiveMeetingByTitle` + `setVideoRoomIdIfAbsent` — an atomic "only one wins" Mongo update) — the doctor-chat equivalent reuses the identical pattern, since `RealtimeKitClient` is fully generic already (title-string based, no participant-role logic in it at all).

---

## Backend (`SmartRoundClinicBackend`)

Kotlin/Ktor/MongoDB modular monolith, `:auth` is this repo's reference-convention module. Cross-module reads without a Gradle dependency go through `data/lookup/XxxLookup.kt`; cross-module writes/live queries go through either a real Gradle dependency or a narrow injected interface (the existing `NotificationSender?`-style nullable-cross-module-callback idiom).

### New `:referral` module
Depends on `:common`, `:infra`, `:scheduling`, `:doctor`, `:medical-records`.
- `data/entity/ReferralEntity.kt`: `id, sourceAppointmentId, referringDoctorId, referringDoctorName, referringDoctorPicture (denormalized snapshot, see Flow 1 edge cases), patientId, receivingDoctorId, reason, status ("PENDING"|"ACCEPTED"|"DECLINED"), resultingAppointmentId?, createdAt, respondedAt?`. New `MongoDBConstants.REFERRALS = "referrals"`.
- `domain/repository/ReferralRepository.kt` / impl: `create`, `getById`, `getByReferringDoctor(doctorId)`, `getActiveBySourceAppointment(appointmentId)` (for the "one active referral per appointment" check), `accept(id, patientId)`, `decline(id, patientId)`, `linkResultingAppointment(id, appointmentId)`, `getAllForAdmin(page, size, status?)`.
- `domain/usecase/ReferralEligibilityUseCase.kt`: `appointment.status=="COMPLETED"` (via `AppointmentRepository`) `&&` a rating exists for the appointment (via `DoctorRatingRepository` — **add `getByAppointmentId(appointmentId)`**, doesn't exist today) `&&` a medical record exists with non-empty `prescription` and non-blank `summary` (via `MedicalRecordRepository.getByAppointmentId`, already exists). Exposed standalone as `GET /referral/eligibility?appointmentId=` (kept standalone, not embedded in `AppointmentRes`, to avoid a circular `:scheduling → :referral` Gradle dependency).
- `domain/usecase/CreateReferralUseCase.kt`: eligibility check, both doctors verified (`VerifiedDoctorResolver`), `receivingDoctorId != referringDoctorId`, no existing active referral for the source appointment (checked via `ReferralRepository.getActiveBySourceAppointment`, not via any field on the appointment itself — see the data-model note below), snapshot the referring doctor's current name/picture onto the entity, fire `REFERRAL_CREATED` (recipient: the receiving doctor gets no notification at creation time — only the patient does, per the CR's original notify-patient flow; this round doesn't build the patient-side accept/decline UI, but the backend endpoints exist so the patient app round can wire them next). **Does not touch `AppointmentEntity` at all** — see data-model note below.
- `domain/usecase/AcceptReferralUseCase.kt` / `DeclineReferralUseCase.kt`: caller must be the referral's patient, status must be PENDING. Accept flips to ACCEPTED and fires `REFERRAL_ACCEPTED` to the referring doctor; Decline flips to DECLINED and fires `REFERRAL_DECLINED` similarly. Accept does **not** create an appointment — only unlocks the normal booking flow with the target doctor pre-selected (confirmed against CR §5.1.3).
- `domain/usecase/GetMyReferralsUseCase.kt` (doctor's sent list — used by a future "referrals I sent" view if wanted; not required for Flow 1's Referral tab, which reads from the appointments endpoint instead), admin use cases `GetAdminReferralsUseCase`/`GetAdminReferralStatsUseCase` → `ReferralStatsRes(total, accepted, declined, pending, acceptanceRate)`.
- `presentation/controller/ReferralController.kt`, `route("/referral")` under `authenticate("auth-jwt")`:
  - `POST /referral` (DOCTOR), `GET /referral/mine` (DOCTOR), `GET /referral/pending` (PATIENT — next round), `PATCH /referral/{id}/accept` (PATIENT — next round), `PATCH /referral/{id}/decline` (PATIENT — next round), `GET /referral/eligibility?appointmentId=` (DOCTOR), `GET /referral/admin?page=&size=&status=` (ADMIN), `GET /referral/admin/stats` (ADMIN).
- `koin/ReferralKoinModule.kt` — flat `single{}` list, same style as `SchedulingKoinModule`.

### `:scheduling` additions (minimal, additive)

**Data-model note (important, confirmed with user):** referrals live entirely in their own `referrals` MongoDB collection (`:referral` module, above). The `appointments` collection gains exactly **one** new field — a nullable `referralId` — and nothing else. An appointment only ever gets `referralId` populated **once, at the moment the patient successfully books with the receiving doctor** (i.e. on the *resulting* appointment). The *source* appointment (the original completed visit that made the referral eligible) is never tagged — it's already linked from the referral side via `Referral.sourceAppointmentId`, and the "one active referral per source appointment" check queries the referral collection directly, not the appointment. So: `Referral` points at its source appointment; the resulting appointment points back at its `Referral` — a one-way pointer in each direction, never both stamped onto the same appointment record.

- `AppointmentEntity.kt`/`Appointment.kt` (confirmed current shape has no referral field): add `val referralId: String? = null` — this is the only schema change needed on the appointment side.
- `AppointmentRepository.kt`: add `suspend fun setReferralId(appointmentId: String, referralId: String): Resource<Boolean>`, called from exactly one place: `BookAppointmentUseCase`, below, on the newly-created appointment.
- `AppointmentRes.kt` (used by `GET /scheduling/appointments/doctor/all`, the doctor's own list — this is what Flow 1's Referral tab actually reads): add `referralId: String?`, `referredByDoctorName: String?`, `referredByDoctorPicture: String?` — the latter two are **response-only enrichment**, resolved at read-time via a lookup-by-id read against the `referrals` collection (cheap single-doc fetch using the denormalized snapshot fields already stored on `ReferralEntity` — no join needed), never persisted on the appointment document itself. `AdminAppointmentRes` gets `referralId`/`referralStatus` too, for admin visibility (backend-only this round, no admin UI in this workspace).
- `BookAppointmentReq.kt`: add `val referralId: String? = null`. `BookAppointmentUseCase`: when present, validate the referral is ACCEPTED, `receivingDoctorId == req.doctorId`, `patientId == caller`; after booking succeeds, call `setReferralId` on the newly-created appointment (this is the one and only place `Appointment.referralId` is ever written) and call a narrow injected `ReferralLinker` interface (new, in `:common`, implemented in `:referral`, injected nullable — mirrors the existing `NotificationSender?` idiom) to flip `resultingAppointmentId` on the referral. (Needed for the next round's patient-side booking flow — build it now alongside the rest of `:scheduling`'s changes since it's a small additive change to the same files.)

### Verified-doctor gate, notifications
- `common/VerifiedDoctorResolver.kt` (new interface, mirrors the existing `DoctorSpecialitiesResolver` pattern): `suspend fun isVerified(doctorId: String): Boolean`. Implemented in `:doctor`, extracting the currently inline/duplicated logic in `RecommendationRepositoryImpl.loadVerifiedDoctorIds()`/`loadSuspendedDoctorIds()` (`isApproved && isMonetized` from `doctor_compliance`, minus suspended accounts from `auth_user`). Used by `CreateReferralUseCase` and `InitiateDoctorChatUseCase`.
- `common/PushNotificationEvents.kt`: add `REFERRAL_CREATED`, `REFERRAL_ACCEPTED`, `REFERRAL_DECLINED`, `NEW_DOCTOR_CHAT_MESSAGE`, plus doctor-chat call-signaling constants mirroring the existing `CALL_DOCTOR_JOINED`-style constants (exact naming to match whatever the full existing `CALL_*` set turns out to be when this file is opened for editing).
- Admin routes need no new `Permission` entries — this repo's permission catalog is route-derived/auto-synced (`PermissionCatalogSync`); new `/referral/admin*` routes auto-register, and `call.requireRole(ADMIN)` (the pattern `AdminAppointmentController` already uses) is the right guard.
- `GET /doctor/recommendations`: add optional `excludeDoctorId` query param, threaded through `RecommendationController.kt` → `GetRecommendedDoctorsUseCase` → `RecommendationRepositoryImpl` (simple additive filter on the existing query).
- Add `DoctorRatingRepository.getByAppointmentId(appointmentId)` (doesn't exist today — only `getById`/`getByDoctorId` do).

### New `:doctor-chat` module (parallel to `:consultation`, full call parity)
Depends on `:common`, `:infra`, `:doctor` (for `VerifiedDoctorResolver`).
- `data/entity/DoctorChatThreadEntity.kt`: `{id, doctorAId, doctorBId, videoRoomId?, lastVideoRoomId?, createdAt, updatedAt}` — pair-key deduped via sorted `(doctorAId, doctorBId)` so `(A,B)` and `(B,A)` resolve to one thread. Includes `videoRoomId`/`lastVideoRoomId` from the start, mirroring `ConsultationThreadEntity`, since calling is in scope this round.
- `data/entity/DoctorChatMessageEntity.kt`: `{id, threadId, senderId, senderName, messageType(TEXT/FILE), message?, files: List<...>, createdAt}` — same file-message shape as `ConsultationMessageEntity`, duplicated per the parallel-module decision rather than shared.
- New collections `doctor_chat_threads`, `doctor_chat_messages`.
- `domain/service/DoctorChatService.kt` — same MongoDB change-stream broadcast pattern as `ConsultationChatService`; file storage via the existing `StorageRepository`/R2 (`:infra`), key prefix `doctor-chat-files/{threadId}/{messageId}.{ext}`.
- `domain/service/DoctorChatSocketRegistry.kt` — same shape as `ConsultationSocketRegistry`, keyed by `threadId`.
- `domain/usecase/InitiateDoctorChatUseCase.kt` — both parties verified, `callerId != otherDoctorId`, find-or-create by pair key (idempotent, see Flow 3 edge cases).
- **Calling** — mirror the `:consultation` call use cases against the same shared `RealtimeKitClient` (`:infra`, already fully generic — takes a plain `title: String`, no doctor/patient-specific logic in it at all, so it's reused unmodified):
  - `JoinDoctorCallUseCase` / `InviteToDoctorCallUseCase` / `DeclineDoctorCallUseCase` / `CancelDoctorCallUseCase` / `EndDoctorCallUseCase`, meeting title convention `"DoctorsLounge $threadId"` (the "doctors_lounge" naming the user asked for — expressed as a per-thread meeting title prefix, matching the existing `"Thread $doctorId:$patientId"` convention exactly, not a single shared room for all doctors).
  - Same atomic `setVideoRoomIdIfAbsent`/`getByVideoRoomId`/`clearVideoRoomId` methods on a new `DoctorChatThreadRepository`, preventing call-join races exactly like the existing consultation path.
  - **Webhook dispatch problem**: Cloudflare's meeting-ended webhook (`POST /webhooks/cloudflare/realtime-kit`) is a single configured endpoint — it can't be registered twice. Resolve by defining a small `common/VideoRoomEventHandler` interface (`suspend fun handleMeetingEnded(meetingId: String): Boolean`, returns whether it recognized the room), Koin-multibinding both `:consultation`'s and `:doctor-chat`'s implementations, and having the existing webhook controller iterate the bound list until one returns `true`. This avoids giving the webhook module a hard dependency on `:doctor-chat` while still supporting both room types on one endpoint.
  - Same generalization for the existing `StaleCallCleanupTask` (abandoned-call cleanup) — accept a Koin-multibound list of cleanup targets rather than duplicating a second cron task.
  - APNs VoIP / FCM incoming-call push reuses the existing `ApnsVoipClient`/data-payload mechanism (`:infra`), just with the new doctor-chat event constants and a `threadId` instead of `otherUserId` in the payload metadata.
- `presentation/controller/DoctorChatController.kt`: `POST /doctor-chat/threads` (initiate), `GET /doctor-chat/threads` (list), `GET /doctor-chat/threads/{threadId}/messages?before=&size=`, `POST /doctor-chat/threads/{threadId}/files`, `WS /doctor-chat/threads/{threadId}`, `POST /doctor-chat/threads/{threadId}/call/{join|invite|decline|cancel|end}` — same route shape as `:consultation`'s chat controller, doctor-chat-scoped.
- Doctor-directory search (used by both Flow 1's referral picker and Flow 3/4's chat picker): reuse `GET /doctor/recommendations?specializationId=&excludeDoctorId=&page=&size=` as-is.

---

## Assumptions / decisions flagged for audit

| # | Decision | Rationale |
|---|---|---|
| 1 | Doctor-doctor chat is a fully parallel module/screens, not a generalization of `:consultation` | Avoids regressing actively-developed, working patient-doctor call code (user-confirmed) |
| 2 | Referrals get a Decline state, not just Pending→Accepted | Clean terminal state instead of indefinite Pending (user-confirmed) |
| 3 | Doctor-to-doctor calling is in scope, via per-thread `"DoctorsLounge {threadId}"` RealtimeKit meetings | User explicitly overrode CR §3.2's "no calling" out-of-scope item |
| 4 | Referral tab shows **all** statuses of referred-in appointments (not just upcoming) | No qualifier given; a complete history reads as more useful than a filtered subset — easy to narrow later if wrong |
| 5 | `Appointment` gains exactly one referral-related field (`referralId`, nullable), written only once — on the resulting appointment, at successful booking time. Referring-doctor name/photo live only on the `Referral` document (own collection) and are joined in at read-time for display, never persisted onto the appointment itself | User-confirmed: referral is its own collection; an appointment only "becomes" referral-tagged when the patient books with the referred doctor, so the appointment side needs nothing beyond that single nullable pointer |
| 6 | Multi-hop referral chaining is allowed (a referred-in appointment can itself be referred onward) | Nothing in the CR forbids it; blocking would need an explicit rule not present |
| 7 | "Refer" (referring a patient **out**) keeps its original CR placement — CTA on a completed appointment under Consultation → Past | The user's correction addressed *viewing* referred-in appointments, not this initiation flow; carried over unchanged |
| 8 | Admin-side referral visibility is backend-API-only this round (no admin frontend repo in this workspace) | Confirmed with user |
| 9 | Patient-side work (Referrals tab, Accept/Decline UI, Payments relocation) is explicitly deferred to a follow-up round | User-confirmed — backend endpoints for accept/decline/pending are still built now so nothing blocks that next round |

---

## Build sequencing

1. **Backend foundation**: `:referral` module skeleton, `ReferralEligibilityUseCase`, `AppointmentEntity.referralId` + doctor-facing `AppointmentRes` enrichment, `VerifiedDoctorResolver` extraction, `excludeDoctorId` on recommendations. Unit-testable in isolation.
2. **Doctor app — Flow 3 (Services port)**: standalone, no dependency on referral or chat being done — testable via the picker screen alone once wired to a stub "Connect"/"Refer to this doctor" callback.
3. **Doctor app — Flow 2 (Wallet relocation)**: fully independent, no backend change, can land any time, in parallel with everything else.
4. **Doctor app — Flow 1 (Bookings restructure + Refer-out flow)**: depends on step 1 (backend) and step 2 (doctor picker UI).
5. **Backend `:doctor-chat` module (text + files first, calling second)**: build message/thread plumbing and verify end-to-end before layering in the call use cases and webhook multiplexing — calling is the highest-complexity slice (Cloudflare integration, webhook dispatch, stale-call cleanup) and benefits from a working text-chat foundation to test against first.
6. **Doctor app — Flow 4 (Chat tab split + doctor conversation/call screens)**: depends on step 5.

---

## Testing / verification

- **Backend**: unit tests for `ReferralEligibilityUseCase` (all combinations of completed/rating/medical-record presence, mirroring `:scheduling`'s existing `SlotEngineTest.kt` style — the only test precedent in that module); unit tests for `CreateReferralUseCase`/`AcceptReferralUseCase`/`DeclineReferralUseCase` with fake repos; unit tests for `InitiateDoctorChatUseCase`'s find-or-create idempotency; manual curl/Postman verification of the referral create→(eligibility re-check)→admin-stats chain, and the doctor-chat thread/message/call lifecycle including a simulated webhook call.
- **Doctor app** (no existing UI test infra): manual checklist —
  1. Complete an appointment, submit rating, submit medical record with prescription+summary → Refer CTA appears (and does *not* appear if any of the three is missing).
  2. Refer → reason → doctor search (verified-only, self-excluded) → confirm → appointment reflects `referralId` once the (future, next-round) patient side books — for this round, verify via a manually-booked test appointment with `referralId` set directly, that it surfaces correctly tagged in the Referral tab with the right "Referred by" avatar/caption.
  3. Bookings screen: Consultation tab's Upcoming/Past sub-tabs behave identically to today; Referral tab shows only tagged appointments, across all statuses; tapping any card opens the same detail flow as a normal appointment.
  4. Wallet menu item under Profile, below Banking Details, opens the unchanged Wallet screen; bottom nav no longer shows a Wallet tab.
  5. Services tab (between Home and Bookings): search, specialty grid, doctor list with chat-icon CTA (direct connect) vs. card-tap (profile, no Specialty/Pricing tab, "Chat" button at bottom) — both paths land in the same thread.
  6. Chat tab: Consultations sub-tab unchanged; Other Doctors sub-tab lists doctor threads, search icon transitions to a search bar, text/file/image exchange works, and a full audio/video call (both directions — invite, join, decline, cancel, end) works end-to-end via the new `doctors_lounge`-style RealtimeKit rooms, with no regression to any existing patient-doctor call in parallel testing.
  7. An unverified/pending-compliance doctor never appears as a referral target or a chat target (seed a non-approved doctor and confirm exclusion in both Flow 1's picker and Flow 3/4's search).
