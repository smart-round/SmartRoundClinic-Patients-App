package ke.co.smartroundclinic.patient.presentation.signup.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SignUpRoot : NavKey

@Serializable
data object SignUp : NavKey

@Serializable
data class AccountVerification(val email: String) : NavKey
