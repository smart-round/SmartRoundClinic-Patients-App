package ke.co.smartroundclinic.patient.presentation.main.home.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object HomeScreen : NavKey
@Serializable data object Notifications : NavKey
@Serializable data object UserProfile : NavKey
@Serializable data object AllDoctors : NavKey
@Serializable data class HomeDoctorsBySpeciality(val specialityId: String, val specialityName: String) : NavKey
@Serializable data class HomeDoctorProfile(val doctorId: String) : NavKey
@Serializable data object HomeDoctorArticleDetail : NavKey
@Serializable data class HomeBookAppointment(val doctorId: String, val previousAppointmentId: String? = null) : NavKey
@Serializable data class HomeAppointmentDetails(val appointmentId: String) : NavKey