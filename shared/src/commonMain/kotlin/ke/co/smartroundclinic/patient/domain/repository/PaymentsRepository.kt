package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.StkPushPreBookingReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentByAppointmentRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryByIdRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.StkPushInitiationData
import ke.co.smartroundclinic.patient.data.remote.dto.response.StkPushStatusData

interface PaymentsRepository {
    suspend fun stkPushPreBooking(body: StkPushPreBookingReq, isRebooking: Boolean = false): Resource<StkPushInitiationData>
    suspend fun getStkPushStatus(invoiceId: String): Resource<StkPushStatusData>
    suspend fun getPaymentsHistory(page: Int, size: Int): Resource<GetPaymentHistoryRes>
    suspend fun getPaymentHistoryById(id: String): Resource<GetPaymentHistoryByIdRes>
    suspend fun getPaymentByAppointment(appointmentId: String): Resource<GetPaymentByAppointmentRes>
}
