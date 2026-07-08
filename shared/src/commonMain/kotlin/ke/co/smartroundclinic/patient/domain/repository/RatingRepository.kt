package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.RatingListResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.RatingResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.SuccessRes

interface RatingRepository {
    suspend fun submitRating(appointmentId: String, doctorId: String, rating: Int, comment: String?): Resource<RatingResponse>
    suspend fun updateRating(id: String, rating: Int?, comment: String?): Resource<RatingResponse>
    suspend fun deleteRating(id: String): Resource<SuccessRes>
    suspend fun getDoctorRatings(doctorId: String, page: Int = 1, size: Int = 20): Resource<RatingListResponse>
}
