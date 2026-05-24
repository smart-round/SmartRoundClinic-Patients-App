package ke.co.smartroundclinic.patient.data.repository

import ke.co.smartroundclinic.patient.core.database.dao.SpecialityDao
import ke.co.smartroundclinic.patient.core.database.entity.SpecialityEntity
import ke.co.smartroundclinic.patient.domain.repository.SpecialityLocalRepository

class SpecialityLocalRepositoryImpl(private val dao: SpecialityDao) : SpecialityLocalRepository {
    override suspend fun getSpecialities(): List<SpecialityEntity> = dao.getAll()
    override suspend fun saveSpecialities(entities: List<SpecialityEntity>) = dao.upsertAll(entities)
    override suspend fun hasSpecialities(): Boolean = dao.getAll().isNotEmpty()
}
