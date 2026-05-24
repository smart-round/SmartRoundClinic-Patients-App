package ke.co.smartroundclinic.patient.domain.usecase.appointment

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.repository.AppointmentRepository

class CancelAppointmentUseCase(private val repository: AppointmentRepository) {
    suspend operator fun invoke(id: String, reason: String? = null): Resource<Appointment> =
        repository.cancelAppointment(id, reason)
}
