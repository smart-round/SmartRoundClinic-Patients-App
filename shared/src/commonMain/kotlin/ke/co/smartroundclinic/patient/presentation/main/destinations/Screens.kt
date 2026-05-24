package ke.co.smartroundclinic.patient.presentation.main.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object MainRoot : NavKey

@Serializable
data object Home : NavKey

@Serializable
data object Appointments : NavKey

@Serializable
data object Doctors : NavKey

@Serializable
data object Articles : NavKey

@Serializable
data object Bookings : NavKey

@Serializable
data object Profile : NavKey
