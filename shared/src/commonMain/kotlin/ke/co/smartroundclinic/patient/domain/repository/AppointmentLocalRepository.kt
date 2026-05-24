package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.domain.model.Appointment

interface AppointmentLocalRepository {
    suspend fun getAll(): List<Appointment>
    suspend fun upsertAll(appointments: List<Appointment>)
    suspend fun deleteAll()
}
