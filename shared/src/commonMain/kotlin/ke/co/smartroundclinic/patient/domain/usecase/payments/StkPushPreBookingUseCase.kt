package ke.co.smartroundclinic.patient.domain.usecase.payments

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.StkPushPreBookingReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.StkPushInitiationData
import ke.co.smartroundclinic.patient.domain.repository.PaymentsRepository

class StkPushPreBookingUseCase(private val repository: PaymentsRepository) {
    suspend operator fun invoke(
        doctorId: String,
        phoneNumber: String,
        isRebooking: Boolean = false,
        previousAppointmentId: String? = null,
    ): Resource<StkPushInitiationData> = repository.stkPushPreBooking(
        body = StkPushPreBookingReq(
            doctorId = doctorId,
            phoneNumber = phoneNumber,
            previousAppointmentId = previousAppointmentId,
        ),
        isRebooking = isRebooking,
    )
}
