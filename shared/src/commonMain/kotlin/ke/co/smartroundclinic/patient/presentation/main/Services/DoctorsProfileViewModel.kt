package ke.co.smartroundclinic.patient.presentation.main.Services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetDoctorArticlesUseCase
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetDoctorProfileUseCase
import ke.co.smartroundclinic.patient.domain.usecase.speciality.GetSpecialityPricingUseCase
import kotlinx.coroutines.launch

class DoctorsProfileViewModel(
    private val getDoctorArticlesUseCase: GetDoctorArticlesUseCase,
    private val getDoctorProfileUseCase: GetDoctorProfileUseCase,
    private val getSpecialityPricingUseCase: GetSpecialityPricingUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(DoctorProfileUiState())
        private set

    fun load(doctor: Doctor) {
        viewModelScope.launch {
            uiState = DoctorProfileUiState()

            Napier.d(tag = "DoctorsProfile", message = "Loading doctorId=${doctor.id} specializationId=${doctor.specializationId}")

            val profileJob = launch {
                val next = when (val r = getDoctorProfileUseCase(doctor.id)) {
                    is Resource.Success -> {
                        val data = r.data
                        if (data != null) {
                            Napier.d(tag = "DoctorsProfile", message = "Profile loaded: ${data.bio?.take(40)}")
                            SectionState.Success(data)
                        } else {
                            Napier.e(tag = "DoctorsProfile", message = "Profile returned null body")
                            SectionState.Error("Profile unavailable")
                        }
                    }
                    is Resource.Error -> {
                        Napier.e(tag = "DoctorsProfile", message = "Profile error: ${r.message}")
                        SectionState.Error(r.message ?: "Failed to load profile")
                    }
                    else -> SectionState.Loading
                }
                uiState = uiState.copy(profile = next)
            }

            val pricingJob = launch {
                val specId = doctor.specializationId
                val next = if (specId == null) {
                    Napier.w(tag = "DoctorsProfile", message = "No specializationId — pricing unavailable")
                    SectionState.Unavailable
                } else {
                    when (val r = getSpecialityPricingUseCase(specId)) {
                        is Resource.Success -> {
                            val data = r.data
                            if (data != null) {
                                Napier.d(tag = "DoctorsProfile", message = "Pricing loaded: tier=${data.tierPrice}")
                                SectionState.Success(data)
                            } else {
                                Napier.e(tag = "DoctorsProfile", message = "Pricing returned null body")
                                SectionState.Error("Pricing unavailable")
                            }
                        }
                        is Resource.Error -> {
                            Napier.e(tag = "DoctorsProfile", message = "Pricing error: ${r.message}")
                            SectionState.Error(r.message ?: "Failed to load pricing")
                        }
                        else -> SectionState.Loading
                    }
                }
                uiState = uiState.copy(pricing = next)
            }

            val articlesJob = launch {
                val next = when (val r = getDoctorArticlesUseCase(doctor.id)) {
                    is Resource.Success -> {
                        val data = r.data ?: emptyList()
                        Napier.d(tag = "DoctorsProfile", message = "Articles loaded: ${data.size}")
                        SectionState.Success(data)
                    }
                    is Resource.Error -> {
                        Napier.e(tag = "DoctorsProfile", message = "Articles error: ${r.message}")
                        SectionState.Error(r.message ?: "Failed to load articles")
                    }
                    else -> SectionState.Loading
                }
                uiState = uiState.copy(articles = next)
            }

            profileJob.join()
            pricingJob.join()
            articlesJob.join()
        }
    }
}
