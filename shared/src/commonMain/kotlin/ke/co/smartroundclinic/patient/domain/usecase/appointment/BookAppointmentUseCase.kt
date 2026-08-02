package ke.co.smartroundclinic.patient.domain.usecase.appointment

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.repository.AppointmentRepository

class BookAppointmentUseCase(private val repository: AppointmentRepository) {
    suspend operator fun invoke(
        doctorId: String,
        date: String,
        slotStart: String,
        notes: String? = null,
        transactionRef: String? = null,
        referralId: String? = null,
    ): Resource<Appointment> = repository.bookAppointment(doctorId, date, slotStart, notes, transactionRef, referralId)
}
