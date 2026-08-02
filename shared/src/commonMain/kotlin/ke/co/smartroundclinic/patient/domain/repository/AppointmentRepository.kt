package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.model.NextAppointment

interface AppointmentRepository {
    suspend fun bookAppointment(
        doctorId: String,
        date: String,
        slotStart: String,
        notes: String?,
        transactionRef: String? = null,
        referralId: String? = null,
    ): Resource<Appointment>
    suspend fun getMyAppointments(): Resource<List<Appointment>>
    suspend fun getAppointment(id: String): Resource<Appointment>
    suspend fun cancelAppointment(id: String, reason: String?): Resource<Appointment>
    suspend fun getNextAppointment(otherUserId: String): Resource<NextAppointment?>
}
