package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.AvailabilityRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.CalendarRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.CalendarView
import ke.co.smartroundclinic.patient.domain.repository.AvailabilityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AvailabilityRepositoryImpl(private val client: HttpClient) : AvailabilityRepository {

    override suspend fun getAvailableSlots(doctorId: String, date: String): Resource<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("scheduling/availability") {
                    parameter("doctorId", doctorId)
                    parameter("date", date)
                }.body<AvailabilityRes>()
                if (response.status) Resource.Success(data = response.data, message = response.message)
                else Resource.Error(message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }

    override suspend fun getCalendarView(
        doctorId: String,
        view: String,
        date: String,
    ): Resource<CalendarView> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("scheduling/calendar") {
                parameter("doctorId", doctorId)
                parameter("view", view)
                parameter("date", date)
                parameter("forDoctor", false)
            }.body<CalendarRes>()
            if (response.status) {
                val data = response.data?.toDomain() ?: return@withContext Resource.Error("No calendar data")
                Resource.Success(data = data, message = response.message)
            } else {
                Resource.Error(message = response.message)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}
