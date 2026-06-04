package ke.co.smartroundclinic.patient.presentation.main.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.presentation.auth.ForgotPasswordViewModel
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.ContactSupport
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.CreateNewPassword
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.Faqs
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.PasswordChanged
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.PersonalInfo
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.ProfileList
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.ResetPassword
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.TermsAndConditions
import ke.co.smartroundclinic.patient.presentation.main.profile.destinations.VerifyEmailSecurity
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.CreateNewPasswordScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.FaqsScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.PasswordChangedScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.PersonalInfoScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.ProfileScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.ResetPasswordScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.TermsAndConditionsScreen
import ke.co.smartroundclinic.patient.presentation.main.profile.ui.VerifyEmailSecurityScreen
import ke.co.smartroundclinic.patient.presentation.main.support.SupportRoot
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoot(
    modifier: Modifier = Modifier,
    onAtRootChanged: (Boolean) -> Unit = {},
    onSignOut: () -> Unit = {},
    onExitProfile: (() -> Unit)? = null,
) {
    val backStack = retain { mutableStateListOf<NavKey>(ProfileList) }
    val isAtRoot = backStack.size == 1
    val forgotPasswordViewModel: ForgotPasswordViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()
    val user by profileViewModel.user.collectAsState()

    SideEffect { onAtRootChanged(isAtRoot) }

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
                    onSecuritySettings = { backStack.add(ResetPassword) },
                    onSupport = { backStack.add(ContactSupport) },
                    onFaqs = { backStack.add(Faqs) },
                    onTerms = { backStack.add(TermsAndConditions) },
                    onSignOut = onSignOut,
                    onBack = onExitProfile,
                )
            }
            entry<PersonalInfo> {
                PersonalInfoScreen(onBack = { backStack.removeLastOrNull() })
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
                )
            }
            entry<Faqs> {
                FaqsScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<TermsAndConditions> {
                TermsAndConditionsScreen(onBack = { backStack.removeLastOrNull() })
            }
        },
    )
}