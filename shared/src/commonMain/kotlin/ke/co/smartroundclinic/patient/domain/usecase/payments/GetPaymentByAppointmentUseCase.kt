package ke.co.smartroundclinic.patient.domain.usecase.payments

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentByAppointmentRes
import ke.co.smartroundclinic.patient.domain.repository.PaymentsRepository

class GetPaymentByAppointmentUseCase(private val repository: PaymentsRepository) {
    suspend operator fun invoke(appointmentId: String): Resource<GetPaymentByAppointmentRes> =
        repository.getPaymentByAppointment(appointmentId)
}
