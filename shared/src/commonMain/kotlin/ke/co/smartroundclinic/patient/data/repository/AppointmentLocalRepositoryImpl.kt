package ke.co.smartroundclinic.patient.data.repository

import ke.co.smartroundclinic.patient.core.database.dao.AppointmentDao
import ke.co.smartroundclinic.patient.core.database.entity.toDomain
import ke.co.smartroundclinic.patient.core.database.entity.toEntity
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.repository.AppointmentLocalRepository

class AppointmentLocalRepositoryImpl(private val dao: AppointmentDao) : AppointmentLocalRepository {
    override suspend fun getAll(): List<Appointment> = dao.getAll().map { it.toDomain() }
    override suspend fun upsertAll(appointments: List<Appointment>) = dao.upsertAll(appointments.map { it.toEntity() })
    override suspend fun deleteAll() = dao.deleteAll()
}
