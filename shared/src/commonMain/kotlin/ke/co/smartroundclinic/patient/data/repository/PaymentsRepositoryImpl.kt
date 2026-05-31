package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.PreBookAppointmentReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryByIdRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.PreBookAppointmentRes
import ke.co.smartroundclinic.patient.domain.repository.PaymentsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class PaymentsRepositoryImpl(private val client: HttpClient) : PaymentsRepository {

    override suspend fun prebookAppointment(body: PreBookAppointmentReq, isRebooking: Boolean): Resource<PreBookAppointmentRes> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.post("payments/intasend/pre-booking") {
                    if (isRebooking) parameter("rebooking", "true")
                    setBody(body)
                }.body<PreBookAppointmentRes>()
                if (res.status) Resource.Success(res, res.message) else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to pre-book appointment")
            }
        }

    override suspend fun getPaymentsHistory(page: Int, size: Int): Resource<GetPaymentHistoryRes> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.get("patient/payments") {
                    parameter("page", page)
                    parameter("size", size)
                }.body<GetPaymentHistoryRes>()
                if (res.status) Resource.Success(res, res.message) else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load payment history")
            }
        }

    override suspend fun getPaymentHistoryById(id: String): Resource<GetPaymentHistoryByIdRes> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.get("patient/payments/specific") {
                    parameter("id", id)
                }.body<GetPaymentHistoryByIdRes>()
                if (res.status) Resource.Success(res, res.message) else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load payment details")
            }
        }
}
