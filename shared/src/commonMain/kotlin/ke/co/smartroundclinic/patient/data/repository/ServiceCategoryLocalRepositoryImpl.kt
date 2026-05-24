package ke.co.smartroundclinic.patient.data.repository

import ke.co.smartroundclinic.patient.core.database.dao.ServiceCategoryDao
import ke.co.smartroundclinic.patient.core.database.entity.ServiceCategoryEntity
import ke.co.smartroundclinic.patient.domain.repository.ServiceCategoryLocalRepository

class ServiceCategoryLocalRepositoryImpl(private val dao: ServiceCategoryDao) : ServiceCategoryLocalRepository {
    override suspend fun getCategories(): List<ServiceCategoryEntity> = dao.getAll()
    override suspend fun saveCategories(entities: List<ServiceCategoryEntity>) = dao.upsertAll(entities)
}
