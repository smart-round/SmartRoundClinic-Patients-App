package ke.co.smartroundclinic.patient.presentation.main.Services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.model.CalendarView
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.domain.model.Speciality
import ke.co.smartroundclinic.patient.data.remote.dto.response.PreBookAppointmentData
import ke.co.smartroundclinic.patient.domain.usecase.appointment.BookAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.availability.GetAvailableSlotsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.availability.GetCalendarViewUseCase
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetDoctorsBySpecializationUseCase
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.domain.usecase.payments.PreBookAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.speciality.GetSpecialitiesUseCase
import kotlinx.coroutines.launch

class ServicesViewModel(
    private val getSpecialitiesUseCase: GetSpecialitiesUseCase,
    private val getDoctorsBySpecializationUseCase: GetDoctorsBySpecializationUseCase,
    private val getCalendarViewUseCase: GetCalendarViewUseCase,
    private val getAvailableSlotsUseCase: GetAvailableSlotsUseCase,
    private val bookAppointmentUseCase: BookAppointmentUseCase,
    private val getAppointmentUseCase: GetAppointmentUseCase,
    private val preBookAppointmentUseCase: PreBookAppointmentUseCase,
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

    var calendarView by mutableStateOf<CalendarView?>(null)
        private set

    var availableSlots by mutableStateOf<List<String>>(emptyList())
        private set

    var isLoadingSlots by mutableStateOf(false)
        private set

    // Pre-booking (IntaSend checkout)
    var isPreBooking by mutableStateOf(false)
        private set

    var preBookData by mutableStateOf<PreBookAppointmentData?>(null)
        private set

    var preBookError by mutableStateOf<String?>(null)
        private set

    // Held while the checkout sheet is open; consumed when booking is confirmed
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

    init {
        viewModelScope.launch {
            val result = getSpecialitiesUseCase()
            if (result is Resource.Success) specialities = result.data ?: emptyList()
        }
    }

    fun loadDoctorsBySpeciality(specialityId: String) {
        viewModelScope.launch {
            isLoadingDoctors = true
            specialityDoctors = emptyList()
            when (val result = getDoctorsBySpecializationUseCase(specialityId)) {
                is Resource.Success -> specialityDoctors = result.data ?: emptyList()
                else -> specialityDoctors = emptyList()
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

    fun preBookAppointment(
        doctorId: String,
        date: String,
        slotStart: String,
        isRebooking: Boolean = false,
        previousAppointmentId: String? = null,
    ) {
        viewModelScope.launch {
            isPreBooking = true
            preBookData = null
            preBookError = null
            when (val result = preBookAppointmentUseCase(doctorId, isRebooking, previousAppointmentId)) {
                is Resource.Success -> {
                    pendingDoctorId = doctorId
                    pendingDate = date
                    pendingSlot = slotStart
                    preBookData = result.data?.data
                }
                is Resource.Error -> {
                    val msg = result.message ?: "Could not initiate payment"
                    preBookError = msg
                    snackbarController.show(msg, isError = true)
                }
                else -> {}
            }
            isPreBooking = false
        }
    }

    fun confirmBookingAfterPayment() {
        val doctorId = pendingDoctorId ?: return
        val date = pendingDate ?: return
        val slot = pendingSlot ?: return
        val transactionRef = preBookData?.id
        preBookData = null
        viewModelScope.launch {
            isBooking = true
            bookedAppointment = null
            bookingError = null
            when (val result = bookAppointmentUseCase(doctorId, date, slot, transactionRef = transactionRef)) {
                is Resource.Success -> bookedAppointment = result.data
                is Resource.Error -> {
                    val msg = result.message ?: "Booking failed"
                    bookingError = msg
                    snackbarController.show(msg, isError = true)
                }
                else -> {}
            }
            isBooking = false
        }
    }

    fun dismissCheckout() {
        preBookData = null
        preBookError = null
        pendingDoctorId = null
        pendingDate = null
        pendingSlot = null
    }

    fun clearBookingState() {
        bookedAppointment = null
        bookingError = null
    }

    fun loadAppointmentDetail(id: String) {
        viewModelScope.launch {
            when (val result = getAppointmentUseCase(id)) {
                is Resource.Success -> appointmentDetail = result.data
                else -> {}
            }
        }
    }
}
