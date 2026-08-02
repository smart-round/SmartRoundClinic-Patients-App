package ke.co.smartroundclinic.patient.presentation.main.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.compose.material3.ExperimentalMaterial3Api
import ke.co.smartroundclinic.patient.core.legal.FaqScreen
import ke.co.smartroundclinic.patient.core.legal.PrivacyPolicyScreen
import ke.co.smartroundclinic.patient.core.legal.TermsAndConditionsScreen
import ke.co.smartroundclinic.patient.presentation.auth.ForgotPasswordViewModel
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.About
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.ContactSupport
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.CreateNewPassword
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.Faqs
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.MedicalBio
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.MedicalHistory
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.PasswordChanged
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.PaymentDetail
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.Payments
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.PersonalInfo
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.PrivacyPolicy
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.ProfileList
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.ResetPassword
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.TermsAndConditions
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.VerifyEmailSecurity
import ke.co.smartroundclinic.patient.presentation.main.bookings.ui.PaymentHistoryDetailScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.AboutScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.CreateNewPasswordScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.MedicalBioScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.MedicalHistoryScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.PasswordChangedScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.PaymentsListScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.PersonalInfoScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.ProfileScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.ResetPasswordScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.VerifyEmailSecurityScreen
import ke.co.smartroundclinic.patient.presentation.main.support.SupportRoot
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRoot(
    modifier: Modifier = Modifier,
    onAtRootChanged: (Boolean) -> Unit = {},
    onSignOut: () -> Unit = {},
    onExitProfile: (() -> Unit)? = null,
    pendingSupportTicketId: String? = null,
    onPendingSupportTicketNavigated: () -> Unit = {},
    pendingMedicalHistory: Boolean = false,
    onPendingMedicalHistoryNavigated: () -> Unit = {},
) {
    val backStack = retain { mutableStateListOf<NavKey>(ProfileList) }
    val isAtRoot = backStack.size == 1
    val forgotPasswordViewModel: ForgotPasswordViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()
    val user by profileViewModel.user.collectAsState()

    SideEffect { onAtRootChanged(isAtRoot) }

    LaunchedEffect(pendingSupportTicketId) {
        if (!pendingSupportTicketId.isNullOrBlank()) {
            if (backStack.none { it is ContactSupport }) backStack.add(ContactSupport)
        }
    }

    LaunchedEffect(pendingMedicalHistory) {
        if (pendingMedicalHistory) {
            if (backStack.none { it is MedicalHistory }) backStack.add(MedicalHistory)
            onPendingMedicalHistoryNavigated()
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) backStack.removeLastOrNull()
            else onExitProfile?.invoke()
        },
        entryProvider = entryProvider {
            entry<ProfileList> {
                ProfileScreen(
                    onPersonalInfo = { backStack.add(PersonalInfo) },
                    onPayments = { backStack.add(Payments) },
                    onMedicalInfo = { backStack.add(MedicalBio) },
                    onMedicalHistory = { backStack.add(MedicalHistory) },
                    onSecuritySettings = { backStack.add(ResetPassword) },
                    onSupport = { backStack.add(ContactSupport) },
                    onAbout = { backStack.add(About) },
                    onSignOut = onSignOut,
                    onBack = onExitProfile,
                )
            }
            entry<PersonalInfo> {
                PersonalInfoScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<Payments> {
                PaymentsListScreen(
                    onPaymentClick = { backStack.add(PaymentDetail(it)) },
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<PaymentDetail> { dest ->
                PaymentHistoryDetailScreen(
                    paymentId = dest.paymentId,
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<ResetPassword> {
                ResetPasswordScreen(
                    onProceed = { backStack.add(VerifyEmailSecurity) },
                    onBack = { backStack.removeLastOrNull() },
                    viewModel = forgotPasswordViewModel,
                )
            }
            entry<VerifyEmailSecurity> {
                VerifyEmailSecurityScreen(
                    onContinue = { backStack.add(CreateNewPassword) },
                    onBack = { backStack.removeLastOrNull() },
                    viewModel = forgotPasswordViewModel,
                )
            }
            entry<CreateNewPassword> {
                CreateNewPasswordScreen(
                    onSuccess = {
                        backStack.removeAll { it is ResetPassword || it is VerifyEmailSecurity || it is CreateNewPassword }
                        backStack.add(PasswordChanged)
                    },
                    onBack = { backStack.removeLastOrNull() },
                    viewModel = forgotPasswordViewModel,
                )
            }
            entry<PasswordChanged> {
                PasswordChangedScreen(
                    onOk = { backStack.removeAll { it is PasswordChanged } },
                )
            }
            entry<ContactSupport> {
                SupportRoot(
                    user = user,
                    onBack = { backStack.removeLastOrNull() },
                    pendingTicketId = pendingSupportTicketId,
                    onPendingNavigated = onPendingSupportTicketNavigated,
                )
            }
            entry<About> {
                AboutScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onFaqs = { backStack.add(Faqs) },
                    onTerms = { backStack.add(TermsAndConditions) },
                    onPrivacyPolicy = { backStack.add(PrivacyPolicy) },
                )
            }
            entry<Faqs> {
                FaqScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<TermsAndConditions> {
                TermsAndConditionsScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<PrivacyPolicy> {
                PrivacyPolicyScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<MedicalBio> {
                val medicalBioViewModel: MedicalBioViewModel = koinViewModel()
                MedicalBioScreen(
                    viewModel = medicalBioViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToPersonalInfo = {
                        if (backStack.none { it is PersonalInfo }) backStack.add(PersonalInfo)
                    },
                )
            }
            entry<MedicalHistory> {
                val medicalHistoryViewModel: MedicalHistoryViewModel = koinViewModel()
                MedicalHistoryScreen(
                    records = medicalHistoryViewModel.records,
                    isLoading = medicalHistoryViewModel.isLoading,
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}