package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.parameters
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetDoctorArticlesRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetDoctorBySpecializationRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetDoctorsProfileRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetDoctorsRecommendationRes
import ke.co.smartroundclinic.patient.domain.repository.DoctorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class DoctorRepositoryImpl(private val client: HttpClient) : DoctorRepository {
    override suspend fun getRecommendedDoctors(): Resource<GetDoctorsRecommendationRes> =
        withContext(Dispatchers.IO) {
            try {
                val response =
                    client.get("doctor/recommendations").body<GetDoctorsRecommendationRes>()
                Resource.Success(data = response, message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }

    override suspend fun getDoctorBySpecialization(
        specializationId: String,
        page: Int,
        size: Int
    ): Resource<GetDoctorBySpecializationRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("/doctor/specializations/doctors"){
                parameter(key = "specializationId", value = specializationId)
                parameter(key = "page", value = page)
                parameter(key = "size", value = size)
                }.body<GetDoctorBySpecializationRes>()
            Resource.Success(data = response, message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getDoctorProfile(id: String): Resource<GetDoctorsProfileRes> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get("doctor/profile/me") {
                    parameter(key = "id", value = id)
                }.body<GetDoctorsProfileRes>()
                Resource.Success(data = response, message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    override suspend fun getDoctorArticles(id: String, page: Int, size: Int): Resource<GetDoctorArticlesRes> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get("article/my") {
                    parameter(key = "id", value = id)
                    parameter(key = "page", value = page)
                    parameter(key = "size", value = size)
                }.body<GetDoctorArticlesRes>()
                Resource.Success(data = response, message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

}
