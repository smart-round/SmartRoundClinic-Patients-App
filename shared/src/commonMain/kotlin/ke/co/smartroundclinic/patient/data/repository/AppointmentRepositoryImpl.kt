package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.BookAppointmentReq
import ke.co.smartroundclinic.patient.data.remote.dto.request.CancelAppointmentReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.AppointmentRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.AppointmentsListRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.NextAppointmentRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.model.NextAppointment
import ke.co.smartroundclinic.patient.domain.repository.AppointmentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AppointmentRepositoryImpl(private val client: HttpClient) : AppointmentRepository {

    override suspend fun bookAppointment(
        doctorId: String,
        date: String,
        slotStart: String,
        notes: String?,
        transactionRef: String?,
    ): Resource<Appointment> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("scheduling/appointments") {
                setBody(BookAppointmentReq(doctorId = doctorId, date = date, slotStart = slotStart, notes = notes, transactionRef = transactionRef))
            }.body<AppointmentRes>()
            if (response.status) {
                val data = response.data?.toDomain() ?: return@withContext Resource.Error("No appointment data")
                Resource.Success(data = data, message = response.message)
            } else {
                Resource.Error(message = response.message)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getMyAppointments(): Resource<List<Appointment>> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("scheduling/appointments/patient").body<AppointmentsListRes>()
            if (response.status) Resource.Success(data = response.data.map { it.toDomain() }, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getAppointment(id: String): Resource<Appointment> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("scheduling/appointments") {
                parameter("id", id)
            }.body<AppointmentRes>()
            if (response.status) {
                val data = response.data?.toDomain() ?: return@withContext Resource.Error("No appointment data")
                Resource.Success(data = data, message = response.message)
            } else {
                Resource.Error(message = response.message)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun cancelAppointment(id: String, reason: String?): Resource<Appointment> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.patch("scheduling/appointments/cancel") {
                    parameter("id", id)
                    setBody(CancelAppointmentReq(reason = reason))
                }.body<AppointmentRes>()
                if (response.status) {
                    val data = response.data?.toDomain() ?: return@withContext Resource.Error("No appointment data")
                    Resource.Success(data = data, message = response.message)
                } else {
                    Resource.Error(message = response.message)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }

    override suspend fun getNextAppointment(otherUserId: String): Resource<NextAppointment?> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("scheduling/appointments/next") { parameter("otherUserId", otherUserId) }.body<NextAppointmentRes>()
            if (response.status) Resource.Success(response.data?.toDomain())
            else Resource.Error(response.message.ifBlank { "Failed to load next appointment" })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}
