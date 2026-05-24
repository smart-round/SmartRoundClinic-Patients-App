package ke.co.smartroundclinic.patient.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.presentation.onboarding.destinations.FirstOnBoardingScreen
import ke.co.smartroundclinic.patient.presentation.onboarding.destinations.SecondOnBoardingScreen
import ke.co.smartroundclinic.patient.presentation.onboarding.destinations.ThirdOnBoardingScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingRoot(
    onFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<OnboardingScreenViewModel>()
    val backStack = retain { mutableStateListOf<NavKey>(FirstOnBoardingScreen) }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<FirstOnBoardingScreen> {
                FirstOnboardingScreen(
                    onNavigateToNext = {
                        backStack.add(SecondOnBoardingScreen)
                        backStack.remove(FirstOnBoardingScreen)
                    },
                    onSkipToNext = {
                        viewModel.markOnboardingDone()
                        onFinished()
                    }
                )
            }
            entry<SecondOnBoardingScreen> {
                SecondOnboardingScreen(
                    onNavigateToNext = {
                        backStack.add(ThirdOnBoardingScreen)
                        backStack.remove(SecondOnBoardingScreen)
                    },
                    onSkipToNext = {
                        viewModel.markOnboardingDone()
                        onFinished()
                    }
                )
            }
            entry<ThirdOnBoardingScreen> {
                ThirdOnboardingScreen(
                    onNavigateToNext = {
                        viewModel.markOnboardingDone()
                        onFinished()
                    },
                    onSkipToNext = {
                        viewModel.markOnboardingDone()
                        onFinished()
                    }
                )
            }
        }
    )
}
