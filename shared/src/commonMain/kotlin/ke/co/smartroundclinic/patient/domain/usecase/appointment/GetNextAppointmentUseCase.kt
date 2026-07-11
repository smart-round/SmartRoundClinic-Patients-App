package ke.co.smartroundclinic.patient.domain.usecase.appointment

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.NextAppointment
import ke.co.smartroundclinic.patient.domain.repository.AppointmentRepository

class GetNextAppointmentUseCase(private val repository: AppointmentRepository) {
    suspend operator fun invoke(otherUserId: String): Resource<NextAppointment?> = repository.getNextAppointment(otherUserId)
}
