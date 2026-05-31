package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.PreBookAppointmentReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryByIdRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.PreBookAppointmentRes

interface PaymentsRepository {
    suspend fun prebookAppointment(body: PreBookAppointmentReq, isRebooking: Boolean = false): Resource<PreBookAppointmentRes>
    suspend fun getPaymentsHistory(page:Int,size:Int): Resource<GetPaymentHistoryRes>
    suspend fun getPaymentHistoryById(id: String): Resource<GetPaymentHistoryByIdRes>
}