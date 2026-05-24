package ke.co.smartroundclinic.patient.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Constants.ONBOARDING_KEY
import ke.co.smartroundclinic.patient.domain.usecase.datastore.ObserveKeyUseCase
import ke.co.smartroundclinic.patient.domain.usecase.datastore.SetKeyUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingScreenViewModel(
    private val observeKeyUseCase: ObserveKeyUseCase,
    private val setKeyUseCase: SetKeyUseCase,
) : ViewModel() {

    val isOnboardingCompleted = observeKeyUseCase(ONBOARDING_KEY)
        .map { it?.toBooleanStrictOrNull() == true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun markOnboardingDone() {
        viewModelScope.launch {
            setKeyUseCase(ONBOARDING_KEY, "true")
        }
    }
}
