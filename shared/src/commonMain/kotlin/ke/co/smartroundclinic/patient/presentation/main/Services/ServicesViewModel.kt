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
import ke.co.smartroundclinic.patient.domain.usecase.appointment.BookAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.appointment.CancelAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.availability.GetAvailableSlotsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.availability.GetCalendarViewUseCase
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetDoctorsBySpecializationUseCase
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.domain.usecase.medicalrecord.GetMedicalRecordUseCase
import ke.co.smartroundclinic.patient.domain.usecase.payments.GetStkPushStatusUseCase
import ke.co.smartroundclinic.patient.domain.usecase.payments.StkPushPreBookingUseCase
import ke.co.smartroundclinic.patient.domain.usecase.speciality.GetSpecialitiesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class StkPushResult(
    val invoiceId: String,
    val transactionRef: String,
    val amount: Double,
    val currency: String,
)

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
    private val snackbarController: SnackbarController,
) : ViewModel() {

    var selectedArticle by mutableStateOf<Article?>(null)
    var pendingDoctor by mutableStateOf<Doctor?>(null)

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

    // Held while the STK push sheet is open; consumed when booking is confirmed
    private var pendingDoctorId: String? = null
    private var pendingDate: String? = null
    private var pendingSlot: String? = null

    var isBooking by mutableStateOf(false)
        private set

    var bookedAppointment by mutableStateOf<Appointment?>(null)
        private set

    var bookingError by mutableStateOf<String?>(null)
        private set

    var appointmentDetail by mutableStateOf<Appointment?>(null)
        private set

    var isCancelling by mutableStateOf(false)
        private set

    var medicalRecord by mutableStateOf<MedicalRecord?>(null)
        private set
    var isLoadingMedicalRecord by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val result = getSpecialitiesUseCase()
            if (result is Resource.Success) specialities = result.data ?: emptyList()
        }
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
        viewModelScope.launch {
            calendarView = null
            when (val result = getCalendarViewUseCase(doctorId, view = "month", date = "$yearMonth-01")) {
                is Resource.Success -> calendarView = result.data
                else -> {}
            }
        }
    }

    fun loadSlots(doctorId: String, date: String) {
        viewModelScope.launch {
            isLoadingSlots = true
            availableSlots = emptyList()
            when (val result = getAvailableSlotsUseCase(doctorId, date)) {
                is Resource.Success -> availableSlots = result.data ?: emptyList()
                else -> availableSlots = emptyList()
            }
            isLoadingSlots = false
        }
    }

    fun initiateStkPush(
        doctorId: String,
        date: String,
        slotStart: String,
        phoneNumber: String,
        isRebooking: Boolean = false,
        previousAppointmentId: String? = null,
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
                    stkPushData = StkPushResult(
                        invoiceId = data.invoiceId,
                        transactionRef = data.transactionRef,
                        amount = data.amount,
                        currency = data.currency,
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
                                val transactionRef = stkPushData?.transactionRef
                                stkPushData = null
                                // Wait for the IntaSend webhook to update the backend DB
                                // before booking; without this the booking returns 402 PENDING.
                                delay(2_000)
                                confirmBookingAfterPayment(transactionRef)
                                return@launch
                            }
                            "FAILED" -> {
                                val reason = result.data?.invoice?.failedReason
                                    ?: "Payment was not completed. Please try again."
                                stkError = reason
                                snackbarController.show(reason, isError = true)
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

    private fun confirmBookingAfterPayment(transactionRef: String?) {
        val doctorId = pendingDoctorId ?: return
        val date = pendingDate ?: return
        val slot = pendingSlot ?: return
        viewModelScope.launch {
            isBooking = true
            bookedAppointment = null
            bookingError = null
            var lastError: String? = null
            repeat(4) { attempt ->
                if (attempt > 0) delay(3_000)
                when (val result = bookAppointmentUseCase(doctorId, date, slot, transactionRef = transactionRef)) {
                    is Resource.Success -> {
                        bookedAppointment = result.data
                        isBooking = false
                        return@launch
                    }
                    is Resource.Error -> {
                        lastError = result.message
                        val isPendingPayment = result.message?.contains("PENDING", ignoreCase = true) == true
                        if (!isPendingPayment || attempt == 3) {
                            val msg = result.message ?: "Booking failed"
                            bookingError = msg
                            snackbarController.show(msg, isError = true)
                            isBooking = false
                            return@launch
                        }
                    }
                    else -> {}
                }
            }
            isBooking = false
        }
    }

    fun dismissStkPush() {
        pollJob?.cancel()
        stkPushData = null
        stkError = null
        stkPollState = null
        pendingDoctorId = null
        pendingDate = null
        pendingSlot = null
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
        viewModelScope.launch {
            when (val result = getAppointmentUseCase(id)) {
                is Resource.Success -> appointmentDetail = result.data
                else -> {}
            }
        }
        loadMedicalRecord(id)
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
