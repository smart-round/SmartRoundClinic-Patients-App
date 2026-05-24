package ke.co.smartroundclinic.patient.presentation.main.Services.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object ServicesList : NavKey
@Serializable data class DoctorsByCategory(val categoryId: String, val categoryName: String) : NavKey
@Serializable data class DoctorProfile(val doctorId: String) : NavKey
@Serializable data class BookAppointment(val doctorId: String) : NavKey
@Serializable data class AppointmentDetails(val appointmentId: String) : NavKey
@Serializable data object DoctorArticleDetail : NavKey
