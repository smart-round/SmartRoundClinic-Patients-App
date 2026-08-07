# Manual test plan — Aug 2026 bug fixes + amended screens

Companion to [`bugfix-plan-aug-2026.md`](bugfix-plan-aug-2026.md).

## What is under test

| Build | Ref | State |
|---|---|---|
| Backend | `dev` @ `30c8b16` | Pushed, **NOT deployed to sandbox** |
| Doctor app | `8f23934` + uncommitted | Installed on device |
| Patient app | `259c569` + uncommitted | Installed on device |

Both apps point at `https://sandbox-api.smartroundclinic.co.ke/`.

### ⚠️ Read before starting

**Anything marked 🚫 BLOCKED cannot pass until `dev` is deployed to sandbox.** The
backend repo has no `.github/workflows/`, so that deploy is a manual step. Running
those cases against today's sandbox will produce failures that are *not* app bugs.

**Nothing in this plan has been verified by anyone yet** — the apps compile, launch
without crashing, and the backend unit tests pass. That is all. Every case below is
genuinely unknown.

Two accounts are needed (one patient, one doctor) with a **confirmed** appointment
between them, and a second **completed** appointment for the rating/refer cases.

---

## A. File uploads (Issue 3) — testable now

The `LogLevel.ALL` → `HEADERS` change removed full-body logging on **every** request,
so watch for regressions well beyond chat.

| # | Steps | Expected | ✅/❌ |
|---|---|---|---|
| A1 | Patient → consultation → attach a file **> 25 MB** | Fails **immediately** (no long hang). Bubble shows *"Unable to send file as it is too large. Please try again"* beneath the filename | |
| A2 | Patient → attach a ~5–10 MB image | Uploads, and noticeably faster than the previous build. Appears in the thread | |
| A3 | Patient → attach a small (<1 MB) image | Uploads normally — confirms the timeout/cap changes broke nothing | |
| A4 | Patient → attach a PDF | Uploads, shows as a PDF in the bubble | |
| A5 | Doctor → attach a ~5–10 MB image | Uploads, faster than before | |
| A6 | Doctor → attach a file **> 25 MB** | ⚠️ **Expected to still hang and fail badly** — the cap is not implemented on the doctor app yet. Record the behaviour, don't file it | |
| A7 | Both apps: sign in, load bookings, load chat list, open a profile | Everything still works — the logging change touched the shared HTTP client | |

**Regression risk:** `A7` matters. If any screen that previously worked now fails to
load, suspect the HTTP client change first.

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

## C. Photo previews in chat lists (Issues 4 & 5) — 🚫 BLOCKED on backend deploy

The preview string is produced server-side. Until `dev` is deployed these will all
show the old filename/UUID regardless of the app build.

| # | Steps | Expected after deploy | ✅/❌ |
|---|---|---|---|
| C1 | Patient → chat → **camera** → take + send a photo → back to chat list | Row shows a **camera icon + "Photo"** — not a UUID | |
| C2 | Patient → chat → **gallery** → send an image → back to list | Camera icon + "Photo" — not `filename.png` | |
| C3 | Send a **PDF** → back to list | Filename still shown (`results.pdf`), **no** camera icon | |
| C4 | Send a **text** message → back to list | Message text, no icon | |
| C5 | Doctor app → same four cases in the patient chat list | Same results | |
| C6 | Doctor app → **doctor-to-doctor** chat list | Same results | |
| C7 | Doctor app → **Home** screen recent messages | Camera icon + "Photo" for image threads | |
| C8 | Both apps against the **old** backend | Graceful: previews show filenames, no crash, no blank rows (the field defaults to TEXT) | |

**Note:** the image *inside* the message bubble still shows the raw filename — only
the thread-list preview was changed. Not a bug; flagged as secondary in the bugfix plan.

---

## D. Appointment amount (doctor app) — 🚫 BLOCKED on backend deploy

| # | Steps | Expected after deploy | ✅/❌ |
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
