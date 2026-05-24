package ke.co.smartroundclinic.patient.presentation.main.Services

import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.model.DoctorProfile
import ke.co.smartroundclinic.patient.domain.model.SpecialityPricing

sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data class Success<T>(val data: T) : SectionState<T>
    data class Error(val message: String) : SectionState<Nothing>
    data object Unavailable : SectionState<Nothing>
}

data class DoctorProfileUiState(
    val profile: SectionState<DoctorProfile> = SectionState.Loading,
    val pricing: SectionState<SpecialityPricing> = SectionState.Loading,
    val articles: SectionState<List<Article>> = SectionState.Loading,
)
