package ke.co.smartroundclinic.patient.domain.usecase.servicecategory

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.database.entity.toDomain
import ke.co.smartroundclinic.patient.core.database.entity.toEntity
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.ServiceCategory
import ke.co.smartroundclinic.patient.domain.repository.ServiceCategoryLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.ServiceCategoryRepository

class GetServiceCategoriesUseCase(
    private val remote: ServiceCategoryRepository,
    private val local: ServiceCategoryLocalRepository,
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Resource<List<ServiceCategory>> {
        val cached = local.getCategories()
        if (!forceRefresh && cached.isNotEmpty()) return Resource.Success(cached.map { it.toDomain() })
        return when (val result = remote.getServiceCategories(page = 1, size = 50)) {
            is Resource.Success -> {
                val entities = result.data?.data?.items?.map { it.toDomain().toEntity() } ?: emptyList()
                local.saveCategories(entities)
                Resource.Success(entities.map { it.toDomain() })
            }
            is Resource.Error -> Resource.Error(result.message ?: "Failed to fetch service categories")
            is Resource.Loading -> Resource.Loading()
        }
    }
}
