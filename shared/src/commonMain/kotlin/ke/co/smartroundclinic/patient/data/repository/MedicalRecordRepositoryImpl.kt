package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.MedicalHistoryResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.MedicalRecordResponse
import ke.co.smartroundclinic.patient.domain.repository.MedicalRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class MedicalRecordRepositoryImpl(private val client: HttpClient) : MedicalRecordRepository {

    override suspend fun getByAppointmentId(appointmentId: String): Resource<MedicalRecordResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("medical-records/appointment/$appointmentId").body<MedicalRecordResponse>()
                if (response.status) Resource.Success(data = response, message = response.message)
                else Resource.Error(message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }

    override suspend fun getMyHistory(): Resource<MedicalHistoryResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("medical-records/patient").body<MedicalHistoryResponse>()
                if (response.status) Resource.Success(data = response, message = response.message)
                else Resource.Error(message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
}
