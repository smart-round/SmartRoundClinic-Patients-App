package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.parameters
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetSpecialitiesRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetSpecialityPricingRes
import ke.co.smartroundclinic.patient.domain.repository.SpecialityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SpecialityRepositoryImpl(private val client: HttpClient) : SpecialityRepository {
    override suspend fun getAllSpecialities(): Resource<GetSpecialitiesRes> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("/admin/specialities/all").body<GetSpecialitiesRes>()
                if (response.status) Resource.Success(data = response, message = response.message)
                else Resource.Error(message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }

    override suspend fun getSpecialityPricing(id: String): Resource<GetSpecialityPricingRes> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("/admin/specialities/service-tier/price") {
                    parameter(key = "id", value = id)
                }.body<GetSpecialityPricingRes>()
                if (response.status) Resource.Success(data = response, message = response.message)
                else Resource.Error(message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
}
