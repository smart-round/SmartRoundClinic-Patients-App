package ke.co.smartroundclinic.patient.presentation.main.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.domain.model.Speciality
import ke.co.smartroundclinic.patient.domain.model.User
import ke.co.smartroundclinic.patient.domain.repository.UserLocalRepository
import ke.co.smartroundclinic.patient.domain.usecase.auth.GetUserUseCase
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetRecommendedDoctorsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.speciality.GetSpecialitiesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getRecommendedDoctorsUseCase: GetRecommendedDoctorsUseCase,
    private val getSpecialitiesUseCase: GetSpecialitiesUseCase,
    private val getUserUseCase: GetUserUseCase,
    userLocalRepository: UserLocalRepository,
) : ViewModel() {

    val user: StateFlow<User?> = userLocalRepository.observeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var doctors by mutableStateOf<List<Doctor>>(emptyList())
        private set

    var specialities by mutableStateOf<List<Speciality>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch { getUserUseCase() }
        loadDoctors()
        loadSpecialities()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            getUserUseCase()
            val doctorResult = getRecommendedDoctorsUseCase(forceRefresh = true)
            if (doctorResult is Resource.Success) doctors = doctorResult.data ?: emptyList()
            val specResult = getSpecialitiesUseCase()
            if (specResult is Resource.Success) specialities = specResult.data ?: emptyList()
            isLoading = false
        }
    }

    private fun loadDoctors() {
        viewModelScope.launch {
            isLoading = true
            val result = getRecommendedDoctorsUseCase()
            if (result is Resource.Success) doctors = result.data ?: emptyList()
            isLoading = false
        }
    }

    private fun loadSpecialities() {
        viewModelScope.launch {
            val result = getSpecialitiesUseCase()
            if (result is Resource.Success) specialities = result.data ?: emptyList()
        }
    }
}
