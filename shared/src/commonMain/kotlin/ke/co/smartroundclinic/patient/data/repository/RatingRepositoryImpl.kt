package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.SubmitDoctorRatingReq
import ke.co.smartroundclinic.patient.data.remote.dto.request.UpdateRatingReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.RatingListResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.RatingResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.SuccessRes
import ke.co.smartroundclinic.patient.domain.repository.RatingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class RatingRepositoryImpl(private val client: HttpClient) : RatingRepository {

    override suspend fun submitRating(
        appointmentId: String,
        doctorId: String,
        rating: Int,
        comment: String?,
    ): Resource<RatingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("doctor/ratings") {
                setBody(SubmitDoctorRatingReq(appointmentId, doctorId, rating, comment))
            }.body<RatingResponse>()
            if (response.status) Resource.Success(data = response, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun updateRating(id: String, rating: Int?, comment: String?): Resource<RatingResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.put("doctor/ratings") {
                    parameter("id", id)
                    setBody(UpdateRatingReq(rating, comment))
                }.body<RatingResponse>()
                if (response.status) Resource.Success(data = response, message = response.message)
                else Resource.Error(message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }

    override suspend fun deleteRating(id: String): Resource<SuccessRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("doctor/ratings") {
                parameter("id", id)
            }.body<SuccessRes>()
            if (response.status) Resource.Success(data = response, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getDoctorRatings(doctorId: String, page: Int, size: Int): Resource<RatingListResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("doctor/ratings") {
                    parameter("doctorId", doctorId)
                    parameter("page", page)
                    parameter("size", size)
                }.body<RatingListResponse>()
                if (response.status) Resource.Success(data = response, message = response.message)
                else Resource.Error(message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
}
