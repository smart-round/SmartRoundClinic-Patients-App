package ke.co.smartroundclinic.patient.presentation.main.Services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.model.CalendarView
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.domain.model.Speciality
import ke.co.smartroundclinic.patient.domain.model.MedicalRecord
import ke.co.smartroundclinic.patient.domain.model.Rating
import ke.co.smartroundclinic.patient.domain.usecase.appointment.BookAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.appointment.CancelAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.availability.GetAvailableSlotsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.availability.GetCalendarViewUseCase
import ke.co.smartroundclinic.patient.domain.usecase.datastore.GetKeyUseCase
import ke.co.smartroundclinic.patient.domain.usecase.datastore.SetKeyUseCase
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetDoctorsBySpecializationUseCase
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.domain.usecase.medicalrecord.GetMedicalRecordUseCase
import ke.co.smartroundclinic.patient.domain.usecase.payments.GetStkPushStatusUseCase
import ke.co.smartroundclinic.patient.domain.usecase.payments.StkPushPreBookingUseCase
import ke.co.smartroundclinic.patient.domain.usecase.rating.DeleteDoctorRatingUseCase
import ke.co.smartroundclinic.patient.domain.usecase.rating.RateDoctorUseCase
import ke.co.smartroundclinic.patient.domain.usecase.rating.UpdateDoctorRatingUseCase
import ke.co.smartroundclinic.patient.domain.usecase.speciality.GetSpecialitiesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class StkPushResult(
    val invoiceId: String,
    val transactionRef: String,
    val amount: Double,
    val currency: String,
)

// Persisted client-side (DataStore) from the moment an STK push is *initiated* until the
// resulting booking returns 201, so a killed/backgrounded app — or one whose payment sheet
// was simply closed — can resume instead of losing track of a payment the patient made.
//
// Persisting only from payment completion used to leave a hole: M-Pesa could debit before
// the 3s poll observed it, and closing the sheet in that window discarded the booking with
// no record, so the patient paid again for a slot that still looked free.
@Serializable
data class PendingBookingPayment(
    val doctorId: String,
    val date: String,
    val slotStart: String,
    val transactionRef: String,
    val invoiceId: String? = null,
    val referralId: String? = null,
    /**
     * False while the STK push is still in flight, true once the invoice is confirmed
     * COMPLETE. Only a paid record may drive "you've already paid" UI — see
     * [ServicesViewModel.pendingBookingPayment], which is exposed only when this is true.
     */
    val paid: Boolean = false,
)

private const val ALREADY_RATED_MESSAGE = "You have already rated this appointment"
private const val PENDING_BOOKING_PAYMENT_KEY = "pending_booking_payment"

class ServicesViewModel(
    private val getSpecialitiesUseCase: GetSpecialitiesUseCase,
    private val getDoctorsBySpecializationUseCase: GetDoctorsBySpecializationUseCase,
    private val getCalendarViewUseCase: GetCalendarViewUseCase,
    private val getAvailableSlotsUseCase: GetAvailableSlotsUseCase,
    private val bookAppointmentUseCase: BookAppointmentUseCase,
    private val cancelAppointmentUseCase: CancelAppointmentUseCase,
    private val getAppointmentUseCase: GetAppointmentUseCase,
    private val stkPushPreBookingUseCase: StkPushPreBookingUseCase,
    private val getStkPushStatusUseCase: GetStkPushStatusUseCase,
    private val getMedicalRecordUseCase: GetMedicalRecordUseCase,
    private val rateDoctorUseCase: RateDoctorUseCase,
    private val updateDoctorRatingUseCase: UpdateDoctorRatingUseCase,
    private val deleteDoctorRatingUseCase: DeleteDoctorRatingUseCase,
    private val getKeyUseCase: GetKeyUseCase,
    private val setKeyUseCase: SetKeyUseCase,
    private val snackbarController: SnackbarController,
) : ViewModel() {

    var selectedArticle by mutableStateOf<Article?>(null)
    var pendingDoctor by mutableStateOf<Doctor?>(null)
    // Handed off from a Bookings > Referrals "Accept & Book"/"Book Now" tap (only ever non-null for
    // an ACCEPTED referral — see BookingsRoot's ReferralsTab) so the eventual booking request can
    // tag the resulting appointment with it. Read once at BookAppointment entry, then cleared.
    var pendingReferralId by mutableStateOf<String?>(null)

    private val doctorCache = mutableMapOf<String, Doctor>()

    fun cacheDoctor(doctor: Doctor) { doctorCache[doctor.id] = doctor }

    var specialities by mutableStateOf<List<Speciality>>(emptyList())
        private set

    var specialityDoctors by mutableStateOf<List<Doctor>>(emptyList())
        private set

    var isLoadingDoctors by mutableStateOf(false)
        private set

    var doctorsCurrentPage by mutableStateOf(1)
        private set

    var doctorsTotalPages by mutableStateOf(1)
        private set

    var calendarView by mutableStateOf<CalendarView?>(null)
        private set

    var availableSlots by mutableStateOf<List<String>>(emptyList())
        private set

    var isLoadingSlots by mutableStateOf(false)
        private set

    // STK push payment state
    var isStkInitiating by mutableStateOf(false)
        private set

    var stkPushData by mutableStateOf<StkPushResult?>(null)
        private set

    var stkPollState by mutableStateOf<String?>(null)
        private set

    var stkError by mutableStateOf<String?>(null)
        private set

    private var pollJob: Job? = null
    private var calendarJob: Job? = null
    private var slotsJob: Job? = null

    // Held while the STK push sheet is open; consumed when booking is confirmed
    private var pendingDoctorId: String? = null
    private var pendingDate: String? = null
    private var pendingSlot: String? = null
    private var pendingReferralIdForBooking: String? = null

    var isBooking by mutableStateOf(false)
        private set

    var bookedAppointment by mutableStateOf<Appointment?>(null)
        private set

    var bookingError by mutableStateOf<String?>(null)
        private set

    // Non-null from the moment a payment completes until its booking succeeds (201) or the
    // transactionRef is confirmed spent (409). Retryable — the "book" CTA reuses this ref
    // instead of starting a new STK push while it's set. Only ever holds a *paid* record;
    // an in-flight, not-yet-confirmed payment lives in [inFlightPayment] so it can't make
    // the UI claim the patient has already paid.
    var pendingBookingPayment by mutableStateOf<PendingBookingPayment?>(null)
        private set

    // The payment currently being attempted, paid or not. Persisted to DataStore from
    // initiation so a closed sheet or a killed app can still recover the booking.
    private var inFlightPayment: PendingBookingPayment? = null

    var appointmentDetail by mutableStateOf<Appointment?>(null)
        private set

    var isCancelling by mutableStateOf(false)
        private set

    var medicalRecord by mutableStateOf<MedicalRecord?>(null)
        private set
    var isLoadingMedicalRecord by mutableStateOf(false)
        private set

    var myRatingOfDoctor by mutableStateOf<Rating?>(null)
        private set
    var hasAlreadyRatedAppointment by mutableStateOf(false)
        private set
    var isSubmittingRating by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val result = getSpecialitiesUseCase()
            if (result is Resource.Success) specialities = result.data ?: emptyList()
        }
        viewModelScope.launch { resumePendingBookingPayment() }
    }

    fun loadDoctorsBySpeciality(specialityId: String) {
        doctorsCurrentPage = 1
        doctorsTotalPages = 1
        loadDoctorsPage(specialityId, page = 1)
    }

    fun loadDoctorsPage(specialityId: String, page: Int) {
        viewModelScope.launch {
            isLoadingDoctors = true
            if (page == 1) specialityDoctors = emptyList()
            when (val result = getDoctorsBySpecializationUseCase(specialityId, page)) {
                is Resource.Success -> {
                    val data = result.data?.data ?: return@launch
                    specialityDoctors = data.items.map { it.toDomain() }
                    doctorsCurrentPage = data.page
                    val size = if (data.size > 0) data.size else 20
                    doctorsTotalPages = ((data.total + size - 1) / size).coerceAtLeast(1)
                }
                else -> {}
            }
            isLoadingDoctors = false
        }
    }

    fun doctorById(id: String): Doctor? =
        specialityDoctors.find { it.id == id } ?: doctorCache[id]

    fun loadCalendarView(doctorId: String, yearMonth: String) {
        calendarJob?.cancel()
        calendarJob = viewModelScope.launch {
            calendarView = null
            when (val result = getCalendarViewUseCase(doctorId, view = "month", date = "$yearMonth-01")) {
                is Resource.Success -> calendarView = result.data
                else -> {}
            }
        }
    }

    fun loadSlots(doctorId: String, date: String) {
        slotsJob?.cancel()
        slotsJob = viewModelScope.launch { fetchAvailableSlots(doctorId, date) }
    }

    private suspend fun fetchAvailableSlots(doctorId: String, date: String) {
        isLoadingSlots = true
        availableSlots = emptyList()
        when (val result = getAvailableSlotsUseCase(doctorId, date)) {
            is Resource.Success -> availableSlots = result.data ?: emptyList()
            else -> availableSlots = emptyList()
        }
        isLoadingSlots = false
    }

    fun initiateStkPush(
        doctorId: String,
        date: String,
        slotStart: String,
        phoneNumber: String,
        isRebooking: Boolean = false,
        previousAppointmentId: String? = null,
        referralId: String? = null,
    ) {
        val normalizedPhone = normalizePhone(phoneNumber)
        viewModelScope.launch {
            isStkInitiating = true
            stkError = null
            stkPollState = null
            when (val result = stkPushPreBookingUseCase(
                doctorId = doctorId,
                phoneNumber = normalizedPhone,
                isRebooking = isRebooking,
                previousAppointmentId = previousAppointmentId,
            )) {
                is Resource.Success -> {
                    val data = result.data ?: run {
                        stkError = "Failed to initiate payment"
                        isStkInitiating = false
                        return@launch
                    }
                    pendingDoctorId = doctorId
                    pendingDate = date
                    pendingSlot = slotStart
                    pendingReferralIdForBooking = referralId
                    stkPushData = StkPushResult(
                        invoiceId = data.invoiceId,
                        transactionRef = data.transactionRef,
                        amount = data.amount,
                        currency = data.currency,
                    )
                    // Record the intent *before* the patient can be debited, so closing the
                    // sheet or losing the process can never strand a paid-for booking.
                    persistPendingBookingPayment(
                        PendingBookingPayment(
                            doctorId = doctorId,
                            date = date,
                            slotStart = slotStart,
                            transactionRef = data.transactionRef,
                            invoiceId = data.invoiceId,
                            referralId = referralId,
                            paid = false,
                        ),
                    )
                    startPolling(data.invoiceId)
                }
                is Resource.Error -> {
                    val msg = result.message ?: "Could not initiate payment"
                    stkError = msg
                    snackbarController.show(msg, isError = true)
                }
                else -> {}
            }
            isStkInitiating = false
        }
    }

    private fun startPolling(invoiceId: String) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            repeat(20) {
                delay(3_000)
                when (val result = getStkPushStatusUseCase(invoiceId)) {
                    is Resource.Success -> {
                        val state = result.data?.invoice?.state?.uppercase()
                        stkPollState = state
                        when (state) {
                            "COMPLETE" -> {
                                stkPushData = null
                                // Wait for the IntaSend webhook to update the backend DB
                                // before booking; without this the booking returns 402 PENDING.
                                delay(2_000)
                                confirmBookingAfterPayment(invoiceId)
                                return@launch
                            }
                            "FAILED" -> {
                                val reason = result.data?.invoice?.failedReason
                                    ?: "Payment was not completed. Please try again."
                                stkError = reason
                                snackbarController.show(reason, isError = true)
                                // Definitively not paid — drop the record so it isn't
                                // resumed on next launch.
                                clearPendingBookingPayment()
                                return@launch
                            }
                        }
                    }
                    else -> {}
                }
            }
            // Timed out after ~60 seconds
            stkPollState = "FAILED"
            val msg = "Payment status could not be confirmed. Please check your M-Pesa messages."
            stkError = msg
            snackbarController.show(msg, isError = true)
        }
    }

    /**
     * Promotes the in-flight payment to paid and books against it. Everything is sourced from
     * [inFlightPayment] rather than the sheet's transient state, so this still works when the
     * payment sheet has already been dismissed.
     */
    private fun confirmBookingAfterPayment(invoiceId: String?) {
        val pending = inFlightPayment ?: return
        viewModelScope.launch {
            val paid = pending.copy(invoiceId = invoiceId ?: pending.invoiceId, paid = true)
            persistPendingBookingPayment(paid)
            attemptBooking(paid.doctorId, paid.date, paid.slotStart, paid.transactionRef, paid.referralId)
        }
    }

    /**
     * Re-checks any payment left in flight — a sheet closed mid-payment, or an app killed
     * between the M-Pesa debit and the booking call. Safe to call repeatedly; [attemptBooking]
     * treats a spent transactionRef as terminal, so it cannot double-book.
     */
    fun resumePendingBookingIfAny() {
        if (isBooking) return
        viewModelScope.launch { resumePendingBookingPayment() }
    }

    /**
     * Retries a booking for an already-completed payment against a newly picked date/slot —
     * used after a 400 "slot taken" conflict. Never starts a new STK push.
     */
    fun retryBooking(date: String, slot: String) {
        val pending = pendingBookingPayment ?: return
        if (isBooking) return
        pendingDoctorId = pending.doctorId
        pendingDate = date
        pendingSlot = slot
        pendingReferralIdForBooking = pending.referralId
        viewModelScope.launch {
            persistPendingBookingPayment(pending.copy(date = date, slotStart = slot))
            attemptBooking(pending.doctorId, date, slot, pending.transactionRef, pending.referralId)
        }
    }

    /**
     * On ViewModel/process restart, resumes any payment that completed but never turned into
     * a booking. Guards against re-entering the booking screen mid-burst (which would otherwise
     * create a fresh ViewModel and fire an overlapping retry burst), and re-checks the invoice
     * with IntaSend first rather than blindly hammering the booking endpoint again — a payment
     * left pending across an app restart may genuinely still be unresolved or have failed.
     */
    private suspend fun resumePendingBookingPayment() {
        if (isBooking) return
        val stored = getKeyUseCase(PENDING_BOOKING_PAYMENT_KEY) ?: return
        val pending = runCatching { Json.decodeFromString<PendingBookingPayment>(stored) }.getOrNull()
        if (pending == null) {
            setKeyUseCase(PENDING_BOOKING_PAYMENT_KEY, null)
            return
        }
        inFlightPayment = pending
        // Only a confirmed-paid record may drive the "you've already paid" UI.
        pendingBookingPayment = pending.takeIf { it.paid }
        pendingDoctorId = pending.doctorId
        pendingDate = pending.date
        pendingSlot = pending.slotStart
        pendingReferralIdForBooking = pending.referralId

        val invoiceId = pending.invoiceId
        if (invoiceId == null) {
            // Persisted before invoiceId was tracked — fall back to the old blind-retry behavior.
            markPaidAndBook(pending)
            return
        }
        when (val result = getStkPushStatusUseCase(invoiceId)) {
            is Resource.Success -> {
                when (result.data?.invoice?.state?.uppercase()) {
                    "COMPLETE" -> markPaidAndBook(pending)
                    "FAILED" -> {
                        val reason = result.data?.invoice?.failedReason
                            ?: "Payment was not completed. Please try again."
                        // Only worth interrupting the patient if they were watching this
                        // payment; a stale record recovered on a later launch just goes away.
                        if (pending.paid) {
                            bookingError = reason
                            snackbarController.show(reason, isError = true)
                        }
                        clearPendingBookingPayment()
                        pendingDoctorId = null
                        pendingDate = null
                        pendingSlot = null
                        pendingReferralIdForBooking = null
                    }
                    else -> startPolling(invoiceId) // still PENDING/PROCESSING at IntaSend — keep waiting
                }
            }
            // Couldn't reach IntaSend — assume the optimistic case rather than stranding a
            // payment; a genuinely unpaid ref just fails the booking harmlessly.
            else -> markPaidAndBook(pending)
        }
    }

    private suspend fun markPaidAndBook(pending: PendingBookingPayment) {
        val paid = pending.copy(paid = true)
        persistPendingBookingPayment(paid)
        attemptBooking(paid.doctorId, paid.date, paid.slotStart, paid.transactionRef, paid.referralId)
    }

    private suspend fun persistPendingBookingPayment(pending: PendingBookingPayment) {
        inFlightPayment = pending
        // The UI's "already paid" affordances must never fire on an unconfirmed payment.
        pendingBookingPayment = pending.takeIf { it.paid }
        setKeyUseCase(PENDING_BOOKING_PAYMENT_KEY, Json.encodeToString(pending))
    }

    private suspend fun clearPendingBookingPayment() {
        inFlightPayment = null
        pendingBookingPayment = null
        setKeyUseCase(PENDING_BOOKING_PAYMENT_KEY, null)
    }

    /**
     * Books against a transactionRef that has already been paid. Only two outcomes are terminal:
     * 201 (booked) and 409 "already been used" (transactionRef spent, needs a fresh payment).
     * A 400 "Slot ... is not available" conflict is always retryable with the same
     * transactionRef — it refreshes availability and leaves [pendingBookingPayment] intact so
     * the patient can pick another time without paying again.
     */
    private suspend fun attemptBooking(doctorId: String, date: String, slot: String, transactionRef: String, referralId: String? = null) {
        isBooking = true
        bookedAppointment = null
        bookingError = null
        repeat(4) { attempt ->
            if (attempt > 0) delay(3_000)
            when (val result = bookAppointmentUseCase(doctorId, date, slot, transactionRef = transactionRef, referralId = referralId)) {
                is Resource.Success -> {
                    bookedAppointment = result.data
                    clearPendingBookingPayment()
                    pendingDoctorId = null
                    pendingDate = null
                    pendingSlot = null
                    pendingReferralIdForBooking = null
                    isBooking = false
                    return
                }
                is Resource.Error -> {
                    val msg = result.message ?: "Booking failed"
                    when {
                        msg.startsWith("Slot ") && msg.contains("is not available", ignoreCase = true) -> {
                            snackbarController.show(
                                "Your selected time was just taken — pick another time. You've already paid, no need to pay again.",
                            )
                            pendingSlot = null
                            fetchAvailableSlots(doctorId, date)
                            isBooking = false
                            return
                        }
                        msg.contains("already been used", ignoreCase = true) -> {
                            bookingError = msg
                            snackbarController.show(msg, isError = true)
                            clearPendingBookingPayment()
                            pendingDoctorId = null
                            pendingDate = null
                            pendingSlot = null
                            pendingReferralIdForBooking = null
                            isBooking = false
                            return
                        }
                        msg.contains("PENDING", ignoreCase = true) && attempt < 3 -> Unit
                        else -> {
                            bookingError = msg
                            snackbarController.show(msg, isError = true)
                            isBooking = false
                            return
                        }
                    }
                }
                else -> {}
            }
        }
        isBooking = false
    }

    /**
     * Closes the payment sheet. The sheet is only a window onto the payment — dismissing it
     * must not abandon money that may already have left the patient's account, so while a
     * payment is in flight the poller keeps running and the persisted record stays put. The
     * booking then completes in the background, and a cold start can still recover it.
     */
    fun dismissStkPush() {
        stkPushData = null
        stkError = null
        stkPollState = null
        if (inFlightPayment == null) {
            pollJob?.cancel()
            pendingDoctorId = null
            pendingDate = null
            pendingSlot = null
            pendingReferralIdForBooking = null
        }
    }

    fun clearBookingState() {
        bookedAppointment = null
        bookingError = null
    }

    fun cancelAppointment(id: String, reason: String?) {
        viewModelScope.launch {
            isCancelling = true
            when (val result = cancelAppointmentUseCase(id, reason)) {
                is Resource.Success -> {
                    appointmentDetail = result.data
                    snackbarController.show("Appointment cancelled")
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to cancel appointment", isError = true)
                else -> {}
            }
            isCancelling = false
        }
    }

    fun loadAppointmentDetail(id: String) {
        myRatingOfDoctor = null
        hasAlreadyRatedAppointment = false
        viewModelScope.launch {
            when (val result = getAppointmentUseCase(id)) {
                is Resource.Success -> appointmentDetail = result.data
                else -> {}
            }
        }
        loadMedicalRecord(id)
    }

    fun submitRating(appointmentId: String, doctorId: String, rating: Int, comment: String?) {
        if (isSubmittingRating) return
        viewModelScope.launch {
            isSubmittingRating = true
            when (val result = rateDoctorUseCase(appointmentId, doctorId, rating, comment)) {
                is Resource.Success -> {
                    myRatingOfDoctor = result.data
                    snackbarController.show("Rating submitted")
                }
                is Resource.Error -> {
                    if (result.message == ALREADY_RATED_MESSAGE) hasAlreadyRatedAppointment = true
                    snackbarController.show(result.message ?: "Failed to submit rating", isError = true)
                }
                else -> {}
            }
            isSubmittingRating = false
        }
    }

    fun updateMyRating(rating: Int, comment: String?) {
        val existing = myRatingOfDoctor ?: return
        if (isSubmittingRating) return
        viewModelScope.launch {
            isSubmittingRating = true
            when (val result = updateDoctorRatingUseCase(existing.id, rating, comment)) {
                is Resource.Success -> {
                    myRatingOfDoctor = result.data
                    snackbarController.show("Rating updated")
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to update rating", isError = true)
                else -> {}
            }
            isSubmittingRating = false
        }
    }

    fun deleteMyRating() {
        val existing = myRatingOfDoctor ?: return
        if (isSubmittingRating) return
        viewModelScope.launch {
            isSubmittingRating = true
            when (val result = deleteDoctorRatingUseCase(existing.id)) {
                is Resource.Success -> {
                    myRatingOfDoctor = null
                    snackbarController.show("Rating deleted")
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to delete rating", isError = true)
                else -> {}
            }
            isSubmittingRating = false
        }
    }

    private fun loadMedicalRecord(appointmentId: String) {
        viewModelScope.launch {
            isLoadingMedicalRecord = true
            medicalRecord = null
            when (val result = getMedicalRecordUseCase(appointmentId)) {
                is Resource.Success -> medicalRecord = result.data
                else -> {}
            }
            isLoadingMedicalRecord = false
        }
    }

    private fun normalizePhone(phone: String): String {
        val digits = phone.trim().removePrefix("+").filter { it.isDigit() }
        return when {
            digits.startsWith("0") -> "254${digits.drop(1)}"
            digits.startsWith("254") -> digits
            else -> "254$digits"
        }
    }
}
