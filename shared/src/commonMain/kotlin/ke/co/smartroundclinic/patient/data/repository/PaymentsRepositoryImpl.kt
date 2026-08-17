package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.StkPushPreBookingReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentByAppointmentRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryByIdRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.StkPushInitiationData
import ke.co.smartroundclinic.patient.data.remote.dto.response.StkPushInitiationRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.StkPushStatusData
import ke.co.smartroundclinic.patient.data.remote.dto.response.StkPushStatusRes
import ke.co.smartroundclinic.patient.domain.repository.PaymentsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class PaymentsRepositoryImpl(private val client: HttpClient) : PaymentsRepository {

    override suspend fun stkPushPreBooking(body: StkPushPreBookingReq, isRebooking: Boolean): Resource<StkPushInitiationData> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.post("payments/intasend/pre-booking/stk-push") {
                    if (isRebooking) parameter("rebooking", "true")
                    setBody(body)
                }.body<StkPushInitiationRes>()
                if (res.status && res.data != null) Resource.Success(res.data, res.message)
                else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to initiate STK push")
            }
        }

    override suspend fun getStkPushStatus(invoiceId: String): Resource<StkPushStatusData> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.get("payments/intasend/stk-push/status") {
                    parameter("invoiceId", invoiceId)
                }.body<StkPushStatusRes>()
                if (res.status && res.data != null) Resource.Success(res.data, res.message)
                else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to get payment status")
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

    override suspend fun getPaymentByAppointment(appointmentId: String): Resource<GetPaymentByAppointmentRes> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.get("payments/appointment/$appointmentId").body<GetPaymentByAppointmentRes>()
                if (res.status) Resource.Success(res, res.message) else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load payment")
            }
        }
}
