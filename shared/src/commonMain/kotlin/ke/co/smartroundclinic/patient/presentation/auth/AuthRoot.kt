package ke.co.smartroundclinic.patient.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.top_appbar_logo
import ke.co.smartroundclinic.patient.presentation.auth.destinations.AccountVerification
import ke.co.smartroundclinic.patient.presentation.auth.destinations.CreateNewPassword
import ke.co.smartroundclinic.patient.presentation.auth.destinations.ForgotPassword
import ke.co.smartroundclinic.patient.presentation.auth.destinations.PasswordRestSuccessfully
import ke.co.smartroundclinic.patient.presentation.auth.destinations.SignIn
import ke.co.smartroundclinic.patient.presentation.auth.destinations.VerifyEmail
import ke.co.smartroundclinic.patient.presentation.auth.ui.CreateNewPasswordScreen
import ke.co.smartroundclinic.patient.presentation.auth.ui.ForgotPasswordScreen
import ke.co.smartroundclinic.patient.presentation.auth.ui.PasswordResetSuccessScreen
import ke.co.smartroundclinic.patient.presentation.auth.ui.SignInScreen
import ke.co.smartroundclinic.patient.presentation.auth.ui.VerifyEmailScreen
import ke.co.smartroundclinic.patient.presentation.signup.ui.AccountVerificationScreen
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthRoot(
    onSignUp: () -> Unit = {},
    onSignIn: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val backStack = retain { mutableStateListOf<NavKey>(SignIn) }
    val forgotPasswordViewModel: ForgotPasswordViewModel = koinViewModel()

    fun navigateTo(destination: NavKey) = backStack.add(destination)
    fun navigateBack() = backStack.removeLastOrNull()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (backStack.last() != PasswordRestSuccessfully) {
                TopAppBar(
                    modifier = Modifier.wrapContentHeight(),
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.top_appbar_logo),
                                contentScale = ContentScale.Crop,
                                contentDescription = "Smart Round Clinic",
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (backStack.lastOrNull() == PasswordRestSuccessfully) Modifier
                    else Modifier.padding(paddingValues)
                ),
            backStack = backStack,
            onBack = ::navigateBack,
            entryProvider = entryProvider {
                entry<SignIn> {
                    SignInScreen(
                        onForgotPassword = { navigateTo(ForgotPassword) },
                        onSignUp = onSignUp,
                        onSignIn = onSignIn,
                        onUnverified = { email -> navigateTo(AccountVerification(email)) },
                    )
                }
                entry<AccountVerification> { dest ->
                    AccountVerificationScreen(
                        email = dest.email,
                        onVerify = {
                            backStack.clear()
                            backStack.add(SignIn)
                        },
                        onBack = ::navigateBack,
                        onGoToLogin = {
                            backStack.clear()
                            backStack.add(SignIn)
                        },
                    )
                }
                entry<ForgotPassword> {
                    ForgotPasswordScreen(
                        viewModel = forgotPasswordViewModel,
                        onProceed = { navigateTo(VerifyEmail) },
                        onBack = ::navigateBack,
                    )
                }
                entry<VerifyEmail> {
                    VerifyEmailScreen(
                        viewModel = forgotPasswordViewModel,
                        onContinue = { navigateTo(CreateNewPassword) },
                        onBack = ::navigateBack,
                    )
                }
                entry<CreateNewPassword> {
                    CreateNewPasswordScreen(
                        viewModel = forgotPasswordViewModel,
                        onSuccess = {
                            forgotPasswordViewModel.resetState()
                            navigateTo(PasswordRestSuccessfully)
                        },
                        onBack = ::navigateBack,
                    )
                }
                entry<PasswordRestSuccessfully> {
                    PasswordResetSuccessScreen(
                        onNavigateToLoginScreen = {
                            backStack.clear()
                            backStack.add(SignIn)
                        }
                    )
                }
            }
        )
    }
}
