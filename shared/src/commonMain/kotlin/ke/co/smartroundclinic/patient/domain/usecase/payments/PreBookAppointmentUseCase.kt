package ke.co.smartroundclinic.patient.domain.usecase.payments

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.PreBookAppointmentReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.PreBookAppointmentRes
import ke.co.smartroundclinic.patient.domain.repository.PaymentsRepository

class PreBookAppointmentUseCase(private val repository: PaymentsRepository) {
    suspend operator fun invoke(
        doctorId: String,
        isRebooking: Boolean = false,
        previousAppointmentId: String? = null,
    ): Resource<PreBookAppointmentRes> = repository.prebookAppointment(
        body = PreBookAppointmentReq(doctorId = doctorId, previousAppointmentId = previousAppointmentId),
        isRebooking = isRebooking,
    )
}
