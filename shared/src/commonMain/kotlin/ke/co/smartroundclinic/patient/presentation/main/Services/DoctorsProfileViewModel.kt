package ke.co.smartroundclinic.patient.presentation.main.Services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.domain.model.Rating
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetDoctorArticlesUseCase
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetDoctorProfileUseCase
import ke.co.smartroundclinic.patient.domain.usecase.rating.GetDoctorRatingsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.speciality.GetSpecialityPricingUseCase
import kotlinx.coroutines.launch

class DoctorsProfileViewModel(
    private val getDoctorArticlesUseCase: GetDoctorArticlesUseCase,
    private val getDoctorProfileUseCase: GetDoctorProfileUseCase,
    private val getSpecialityPricingUseCase: GetSpecialityPricingUseCase,
    private val getDoctorRatingsUseCase: GetDoctorRatingsUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(DoctorProfileUiState())
        private set

    var reviews by mutableStateOf<List<Rating>>(emptyList())
        private set
    var isLoadingReviews by mutableStateOf(false)
        private set
    var isLoadingMoreReviews by mutableStateOf(false)
        private set
    var reviewsCurrentPage by mutableStateOf(1)
        private set
    var reviewsTotalPages by mutableStateOf(1)
        private set

    fun loadReviews(doctorId: String) {
        reviewsCurrentPage = 1
        reviewsTotalPages = 1
        reviews = emptyList()
        loadReviewsPage(doctorId, page = 1, append = false)
    }

    fun loadMoreReviews(doctorId: String) {
        if (isLoadingMoreReviews || isLoadingReviews || reviewsCurrentPage >= reviewsTotalPages) return
        loadReviewsPage(doctorId, page = reviewsCurrentPage + 1, append = true)
    }

    private fun loadReviewsPage(doctorId: String, page: Int, append: Boolean) {
        viewModelScope.launch {
            if (append) isLoadingMoreReviews = true else isLoadingReviews = true
            when (val result = getDoctorRatingsUseCase(doctorId, page)) {
                is Resource.Success -> {
                    val data = result.data?.data
                    if (data != null) {
                        val mapped = data.items.map { it.toDomain() }
                        reviews = if (append) reviews + mapped else mapped
                        reviewsCurrentPage = data.page
                        val size = if (data.size > 0) data.size else 20
                        reviewsTotalPages = ((data.total + size - 1) / size).coerceAtLeast(1)
                    }
                }
                else -> {}
            }
            if (append) isLoadingMoreReviews = false else isLoadingReviews = false
        }
    }

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
