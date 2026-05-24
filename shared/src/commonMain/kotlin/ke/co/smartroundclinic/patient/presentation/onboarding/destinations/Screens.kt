package ke.co.smartroundclinic.patient.presentation.onboarding.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object OnBoardingRoot : NavKey

@Serializable
data object FirstOnBoardingScreen : NavKey

@Serializable
data object SecondOnBoardingScreen : NavKey

@Serializable
data object ThirdOnBoardingScreen : NavKey
