package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.core.database.entity.ServiceCategoryEntity

interface ServiceCategoryLocalRepository {
    suspend fun getCategories(): List<ServiceCategoryEntity>
    suspend fun saveCategories(entities: List<ServiceCategoryEntity>)
}
