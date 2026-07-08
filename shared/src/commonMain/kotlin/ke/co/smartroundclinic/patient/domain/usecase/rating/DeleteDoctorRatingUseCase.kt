package ke.co.smartroundclinic.patient.domain.usecase.rating

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.repository.RatingRepository

class DeleteDoctorRatingUseCase(private val repository: RatingRepository) {
    suspend operator fun invoke(id: String): Resource<Unit> =
        when (val result = repository.deleteRating(id)) {
            is Resource.Success -> Resource.Success(Unit, result.message ?: "Success")
            is Resource.Error -> Resource.Error(result.message ?: "An unknown error occurred")
            is Resource.Loading -> Resource.Loading()
        }
}
