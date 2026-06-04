package ke.co.smartroundclinic.patient.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.presentation.auth.AuthRoot
import ke.co.smartroundclinic.patient.presentation.auth.destinations.AuthRoot
import ke.co.smartroundclinic.patient.presentation.main.MainRoot
import ke.co.smartroundclinic.patient.presentation.main.destinations.MainRoot
import ke.co.smartroundclinic.patient.presentation.onboarding.OnboardingRoot
import ke.co.smartroundclinic.patient.presentation.onboarding.destinations.OnBoardingRoot
import ke.co.smartroundclinic.patient.presentation.signup.SignUpRoot
import ke.co.smartroundclinic.patient.presentation.signup.destinations.SignUpRoot
import ke.co.smartroundclinic.patient.presentation.splash.SplashScreen
import ke.co.smartroundclinic.patient.presentation.splash.destinations.SplashScreen

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
) {
    val backStack = retain { mutableStateListOf<NavKey>(SplashScreen) }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<SplashScreen> {
                SplashScreen(
                    onNavigateToOnboarding = {
                        backStack.add(OnBoardingRoot)
                        backStack.remove(SplashScreen)
                    },
                    onNavigateToSignIn = {
                        backStack.add(AuthRoot)
                        backStack.remove(SplashScreen)
                    },
                    onNavigateToDashboard = {
                        backStack.add(MainRoot)
                        backStack.remove(SplashScreen)
                    },
                )
            }
            entry<OnBoardingRoot> {
                OnboardingRoot(
                    onFinished = {
                        backStack.add(AuthRoot)
                        backStack.remove(OnBoardingRoot)
                    }
                )
            }
            entry<SignUpRoot> {
                SignUpRoot(
                    onSignIn = {
                        backStack.add(AuthRoot)
                        backStack.remove(SignUpRoot)
                    }
                )
            }
            entry<AuthRoot> {
                AuthRoot(
                    onSignUp = {
                        backStack.add(SignUpRoot)
                        backStack.remove(AuthRoot)
                    },
                    onSignIn = {
                        backStack.add(MainRoot)
                        backStack.remove(AuthRoot)
                    },
                )
            }
            entry<MainRoot> {
                MainRoot(
                    onSignOut = {
                        backStack.add(AuthRoot)
                        backStack.remove(MainRoot)
                    },
                )
            }
        }
    )
}
