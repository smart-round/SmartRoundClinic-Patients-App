package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Appointment

interface AppointmentRepository {
    suspend fun bookAppointment(
        doctorId: String,
        date: String,
        slotStart: String,
        notes: String?,
        transactionRef: String? = null,
    ): Resource<Appointment>
    suspend fun getMyAppointments(): Resource<List<Appointment>>
    suspend fun getAppointment(id: String): Resource<Appointment>
    suspend fun cancelAppointment(id: String, reason: String?): Resource<Appointment>
}
