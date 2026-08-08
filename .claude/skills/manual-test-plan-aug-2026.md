# Manual test plan — Aug 2026 bug fixes + amended screens

Companion to [`bugfix-plan-aug-2026.md`](bugfix-plan-aug-2026.md).

## What is under test

| Build | Ref | State |
|---|---|---|
| Backend | `dev` @ `add8550` | Pushed and **auto-deployed via Dokploy** |
| Doctor app | `f9de240` | Installed on device |
| Patient app | `9dcda47` | Installed on device |

Both apps point at `https://sandbox-api.smartroundclinic.co.ke/`.

### Deploy status — nothing is blocked any more

`dev` auto-deploys through Dokploy on push, so the backend work is live. Verified by
probing the sandbox: `POST /chat/{id}/files/presign` returns **401** (route exists,
needs auth) while a nonsense path returns **404**.

**The earlier 🚫 BLOCKED markers on sections C and D no longer apply.** Both are
testable now.

### What has actually been confirmed

Only one thing has been confirmed working on a device by a human: **the outgoing-call
avatar (§G3)**. Everything else below is either untested, or verified only as far as
"it compiles, launches without crashing, and unit tests pass" — which is not testing.

Two accounts are needed (one patient, one doctor) with a **confirmed** appointment
between them, and a second **completed** appointment for the rating/refer cases.

---

## A. File uploads (Issue 3) — reworked substantially, needs a full pass

The upload stack was rebuilt during investigation, so this section replaces the
original. Four separate defects were found and fixed:

1. `LogLevel.ALL` was stringifying every request body into the log.
2. Picked files were read **on the UI thread**, freezing the app before anything
   reached the network — this was the actual cause of "it takes forever".
3. The size cap was 25 MiB, so a file the user called "26 MB" (26,000,000 bytes)
   slipped under it.
4. Uploads were proxied through the API, which buffered the whole file in heap and
   then re-uploaded it to R2 — the transfer happened twice, in series.

Now: size checked before reading → read on `Dispatchers.IO` → pre-signed PUT streamed
straight to R2 in 64KB chunks → complete. Ceiling is **300MB**, client and server.

| # | Steps | Expected | ✅/❌ |
|---|---|---|---|
| A1 | Patient → consultation → send a **> 300MB** file | Rejected **instantly**, no freeze. Bubble reads *"Unable to send file as it is too large. Please try again"* | |
| A2 | Patient → send the **26MB mp4** | Uploads. UI stays responsive throughout | |
| A3 | Patient → send a ~2MB image | Uploads quickly; appears in the thread | |
| A4 | Patient → send a PDF | Uploads; shows as a PDF in the bubble | |
| A5 | **Doctor → patient** chat: repeat A1–A3 | Same behaviour — the doctor app now has the cap and streaming too | |
| A6 | **Doctor → doctor** chat: repeat A1–A3 | Same again; this path was the last to be converted | |
| A7 | During a large upload, check the app stays usable (scroll, type) | No freeze — the read is off the UI thread | |
| A8 | Both apps: sign in, load bookings, chat list, profile | Unaffected — the logging change touched the shared HTTP client | |

**Diagnostics:** every upload logs to `SRC-UPLOAD` — start, 10% steps, stored, completed,
with elapsed ms. `adb logcat -s SRC-UPLOAD` will show exactly where time goes, or where
it fails.

---

## A2. Upload progress indicator

Progress silently never moved because `PendingFile.equals` compared only `localId`, so
Compose treated every update as an identical value and skipped recomposition. **The
same bug also broke the "Failed to send" state.**

| # | Steps | Expected | ✅/❌ |
|---|---|---|---|
| A2.1 | Send a large file, watch the bubble | Circular ring fills, percentage **counts up** in the centre | |
| A2.2 | Confirm against logcat | On-screen percentage roughly tracks the `SRC-UPLOAD` 10% lines | |
| A2.3 | Ring does not jump 0 → 100 instantly | If it does, the body is being buffered before transmit — report it | |
| A2.4 | Force a failure (airplane mode mid-upload) | Bubble shows a failure state, not a spinner forever | |
| A2.5 | Repeat A2.1 on doctor→patient and doctor→doctor | Identical behaviour | |

---

## B. Paid booking is no longer lost (Issue 2) — testable now, costs real money

This is the highest-value fix and the riskiest change. It touches live payment code.
**Use the smallest-fee doctor available.** Each case that reaches the PIN prompt is a
real debit.

Check after every case: does **exactly one** appointment exist, and is the slot now
unavailable to others?

| # | Steps | Expected | ✅/❌ |
|---|---|---|---|
| B1 | Book → pay → **let the flow finish untouched** | Booking completes as before. Baseline — proves the fix broke nothing | |
| B2 | Book → pay → **close the sheet ~1–2 s after entering the PIN** | This is the reported bug. Booking still completes in the background. Appointment appears in Bookings; slot consumed. **No second payment needed** | |
| B3 | Book → pay → close the sheet ~5 s after the PIN | Same as B2 | |
| B4 | Book → pay → **force-kill the app immediately** after the PIN → reopen → go to Bookings | Booking is recovered and appears. Only one appointment | |
| B5 | Book → pay → close sheet → **immediately open Bookings** | Recovery fires on entering Bookings; booking appears without a restart | |
| B6 | Book → **cancel at the M-Pesa PIN prompt** (don't pay) | Payment reported as failed. **No** pending record survives — reopening the app must not try to book or claim you've paid | |
| B7 | Book → pay → close sheet → wait, then start a **new** booking with a different doctor | The new flow must show the normal payment sheet, **not** an "already paid" state leaking from B7's predecessor | |
| B8 | Two devices: both pay for the **same slot**; second one loses the race | Loser sees *"Your selected time was just taken — pick another time. You've already paid, no need to pay again."* and can rebook **without paying again** | |

**Regression risks specific to this change:**

- **B7 is the key regression case.** `dismissStkPush` no longer clears the booking
  context while a payment is in flight, so stale state leaking into a *subsequent*
  booking is the most likely way this fix breaks something.
- **B6** covers the other side: a failed payment must clear its record, or the app
  will try to resume a payment that never happened.
- If B2/B3 fail, capture whether the money left the account — that distinguishes
  "poll never saw COMPLETE" from "booking call failed".

---

## C. Photo previews in chat lists (Issues 4 & 5) — ✅ unblocked, untested

The preview string is produced server-side and the backend is now live, so these are
ready to run.

| # | Steps | Expected | ✅/❌ |
|---|---|---|---|
| C1 | Patient → chat → **camera** → take + send a photo → back to chat list | Row shows a **camera icon + "Photo"** — not a UUID | |
| C2 | Patient → chat → **gallery** → send an image → back to list | Camera icon + "Photo" — not `filename.png` | |
| C3 | Send a **PDF** → back to list | Filename still shown (`results.pdf`), **no** camera icon | |
| C4 | Send a **text** message → back to list | Message text, no icon | |
| C5 | Doctor app → same four cases in the patient chat list | Same results | |
| C6 | Doctor app → **doctor-to-doctor** chat list | Same results | |
| C7 | Doctor app → **Home** screen recent messages | Camera icon + "Photo" for image threads | |
| C8 | Both apps against an **older** backend build | Graceful: previews show filenames, no crash, no blank rows (the field defaults to TEXT) | |

**Note:** the image *inside* the message bubble still shows the raw filename — only
the thread-list preview was changed. Not a bug; flagged as secondary in the bugfix plan.

---

## D. Appointment amount (doctor app) — ✅ unblocked, untested

| # | Steps | Expected | ✅/❌ |
|---|---|---|---|
| D1 | Doctor → Bookings → open any appointment | Summary card shows **Amount** and e.g. `KES 750`, matching the doctor's service tier price | |
| D2 | Check an appointment on a **different service tier** | Amount reflects that tier's price, not a hardcoded value | |
| D3 | Against the **old** backend | Amount block is simply **absent** — never `KES 0` or `KES null` | |

**Known risk:** if a service tier's `tierPrice` is stored as a Mongo `Long` or
`Decimal128`, older code would have silently dropped it. Commit `fbba1f7` widened the
read, but this is unverified against real data — **D2 is the case that exercises it.**

---

## E. Amended screen designs — never visually verified

These shipped earlier in the session and **no one has looked at them on a device.**

### E1. Patient → Bookings → Referrals

| # | Check | ✅/❌ |
|---|---|---|
| E1.1 | **Pending** referral: cream card, orange left bar, "Pending" pill | |
| E1.2 | **Accepted**: light green card, green bar, green "Accepted" pill, green-outlined **Book Again** | |
| E1.3 | **Declined**: pink card, red bar, red "Declined" pill, gradient-outlined **Book Now** | |
| E1.4 | Pending actions read **Accept & Book** (green, left) then **Decline Referral** (red outline) | |
| E1.5 | Buttons don't overflow or clip on this device's width | |
| E1.6 | Accept & Book still works end-to-end; Decline still works | |
| E1.7 | Long doctor names ellipsize rather than breaking the layout | |

> **Decision needed:** the card no longer shows the **referral reason** — the design
> has no room for it, so a patient now accepts or declines without seeing why they
> were referred. Confirm that's intended.

> **Also confirm the copy:** design labels Accepted → "Book Again" and Declined →
> "Book Now", which reads backwards. Flagged earlier; left as designed.

### E2. Doctor → Bookings → appointment details

| # | Check | ✅/❌ |
|---|---|---|
| E2.1 | Summary card: accent bar, date/time row with clock chip and dot, divider, 47dp avatar, Bio pill, underlined Medical History | |
| E2.2 | Date reads like **"Wed Jun 20"**, time like **"8:00 - 8:20 AM"** | |
| E2.3 | Status pill is on the **card**, not the app bar | |
| E2.4 | **Confirmed, not yet started**: "Mark As Complete" is **grey**, and the green ⓘ note appears **below both buttons** | |
| E2.5 | **Confirmed, already started**: button is **green**, and the note is **absent** | |
| E2.6 | Bio sheet and Medical History sheet still open | |
| E2.7 | **Completed**: "Need To Refer Your Patient?" card — no accent bar, illustration left, text right, full-width Refer Patient button in the right column | |
| E2.8 | The drawn illustration (two avatars, dashed arcs, circled +) renders sensibly and isn't squashed | |
| E2.9 | **Completed**: single "Rate and Review Patient" card — avatar, teal pencil + red trash, underlined "See All Ratings", stars, Review text | |
| E2.10 | Submitting, editing and deleting a rating all still work | |
| E2.11 | "See All Ratings" opens the ratings sheet | |
| E2.12 | Refer Patient still launches the referral flow | |
| E2.13 | **Booked** status: "Confirm Appointment" + "Cancel Appointment"; cancelling works | |

> **Known behaviour change:** the patient's *aggregate* rating and review count no
> longer appear inline — the merged card dropped them, per the design. They are still
> in the "See All Ratings" sheet. Confirm that's acceptable.

> **Accessibility note:** action buttons are 32dp tall, below the 48dp minimum touch
> target. That is what the design specifies. Check they're comfortably tappable in
> practice.

---

## G. Call screens — avatars

| # | Steps | Expected | ✅/❌ |
|---|---|---|---|
| G1 | Patient calls a doctor; doctor app **open** (WebSocket path) | Incoming-call screen shows the **patient's** avatar | |
| G2 | Patient calls a doctor; doctor app **killed** (push path) | Same — this is separate code from G1, test both | |
| G3 | Caller sees the callee's avatar on the "Calling…" screen | ✅ **CONFIRMED WORKING** | ✅ |
| G4 | Doctor → doctor call, both directions | Avatars on incoming and outgoing | |
| G5 | Counterpart with **no** profile picture | Person-icon placeholder, never a blank circle | |
| G6 | Patient app receiving a doctor's call | ⚠️ **Known gap** — the patient app's incoming-call screen was never wired for `callerPicture`. Backend sends it; client change not done. Expect no avatar | |

---

## F. Cross-cutting regression sweep

The HTTP-client and Room changes have blast radius beyond the fixed screens.

| # | Check | ✅/❌ |
|---|---|---|
| F1 | Doctor app: DB version went 10 → 11 on `fallbackToDestructiveMigration`. **First launch after update clears the local appointment cache** — confirm bookings reload from the network and nothing looks empty or broken | |
| F2 | Both apps: sign out and sign back in | |
| F3 | Both apps: video/voice call still connects | |
| F4 | Both apps: push notification deep links still route correctly | |
| F5 | Patient: book a **normal** appointment (no referral) end to end | |
| F6 | Patient: book from an **accepted referral** — the referral must still be linked | |

---

## Reporting

For each ❌, capture: app + build, account, exact steps, what happened vs. expected,
and `adb logcat` around the failure. For section B failures, **also record whether
money actually left the account** — that single fact separates a polling bug from a
booking-call bug and will save a lot of time.
