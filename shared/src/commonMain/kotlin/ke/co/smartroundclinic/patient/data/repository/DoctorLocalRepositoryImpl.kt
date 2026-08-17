package ke.co.smartroundclinic.patient.data.repository

import ke.co.smartroundclinic.patient.core.database.dao.DoctorDao
import ke.co.smartroundclinic.patient.core.database.entity.DoctorEntity
import ke.co.smartroundclinic.patient.domain.repository.DoctorLocalRepository

class DoctorLocalRepositoryImpl(private val dao: DoctorDao) : DoctorLocalRepository {
    override suspend fun getDoctors(): List<DoctorEntity> = dao.getAll()
    override suspend fun saveDoctors(entities: List<DoctorEntity>) = dao.replaceAll(entities)
}
