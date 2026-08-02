package ke.co.smartroundclinic.patient.presentation.main.bookings.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object BookingsList : NavKey
@Serializable data class BookingAppointmentDetail(val appointmentId: String) : NavKey
@Serializable data class RebookFromBookings(val doctorId: String, val previousAppointmentId: String) : NavKey
