package ke.co.smartroundclinic.patient.presentation.auth.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AuthRoot : NavKey

@Serializable
data object SignIn : NavKey

@Serializable
data object ForgotPassword : NavKey

@Serializable
data object VerifyEmail : NavKey

@Serializable
data object CreateNewPassword : NavKey

@Serializable
data object PasswordRestSuccessfully : NavKey

@Serializable
data class AccountVerification(val email: String) : NavKey
