package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetServiceCategoriesRes
import ke.co.smartroundclinic.patient.domain.repository.ServiceCategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ServiceCategoryRepositoryImpl(private val client: HttpClient) : ServiceCategoryRepository {
    override suspend fun getServiceCategories(page: Int, size: Int): Resource<GetServiceCategoriesRes> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("admin/service-categories/all") {
                    parameter("page", page)
                    parameter("size", size)
                }.body<GetServiceCategoriesRes>()
                Resource.Success(data = response, message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
}
