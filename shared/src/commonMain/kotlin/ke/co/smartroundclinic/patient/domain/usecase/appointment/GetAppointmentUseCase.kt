package ke.co.smartroundclinic.patient.domain.usecase.appointment

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.repository.AppointmentRepository

class GetAppointmentUseCase(private val repository: AppointmentRepository) {
    suspend operator fun invoke(id: String): Resource<Appointment> = repository.getAppointment(id)
}
