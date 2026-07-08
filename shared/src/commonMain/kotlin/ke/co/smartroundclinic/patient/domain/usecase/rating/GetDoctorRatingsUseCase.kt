package ke.co.smartroundclinic.patient.domain.usecase.rating

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.RatingListResponse
import ke.co.smartroundclinic.patient.domain.repository.RatingRepository

class GetDoctorRatingsUseCase(private val repository: RatingRepository) {
    suspend operator fun invoke(doctorId: String, page: Int = 1, size: Int = 20): Resource<RatingListResponse> =
        repository.getDoctorRatings(doctorId, page, size)
}
