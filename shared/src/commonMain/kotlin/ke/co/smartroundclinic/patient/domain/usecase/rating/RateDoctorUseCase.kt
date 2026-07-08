package ke.co.smartroundclinic.patient.domain.usecase.rating

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Rating
import ke.co.smartroundclinic.patient.domain.repository.RatingRepository

class RateDoctorUseCase(private val repository: RatingRepository) {
    suspend operator fun invoke(appointmentId: String, doctorId: String, rating: Int, comment: String?): Resource<Rating> =
        when (val result = repository.submitRating(appointmentId, doctorId, rating, comment)) {
            is Resource.Success -> {
                val data = result.data?.data?.toDomain()
                if (data != null) Resource.Success(data, result.message ?: "Success")
                else Resource.Error("No rating data")
            }
            is Resource.Error -> Resource.Error(result.message ?: "An unknown error occurred")
            is Resource.Loading -> Resource.Loading()
        }
}
