package ke.co.smartroundclinic.patient.presentation.main.profile.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object ProfileList : NavKey
@Serializable data object PersonalInfo : NavKey
@Serializable data object Payments : NavKey
@Serializable data class PaymentDetail(val paymentId: String) : NavKey
@Serializable data object ResetPassword : NavKey
@Serializable data object VerifyEmailSecurity : NavKey
@Serializable data object CreateNewPassword : NavKey
@Serializable data object PasswordChanged : NavKey
@Serializable data object ContactSupport : NavKey
@Serializable data object About : NavKey
@Serializable data object Faqs : NavKey
@Serializable data object TermsAndConditions : NavKey
@Serializable data object PrivacyPolicy : NavKey
@Serializable data object MedicalBio : NavKey
@Serializable data object MedicalHistory : NavKey
