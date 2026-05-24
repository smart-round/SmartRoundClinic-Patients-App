package ke.co.smartroundclinic.patient.domain.usecase.speciality

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.database.entity.toDomain
import ke.co.smartroundclinic.patient.core.database.entity.toEntity
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Speciality
import ke.co.smartroundclinic.patient.domain.repository.SpecialityLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.SpecialityRepository

class GetSpecialitiesUseCase(
    private val remote: SpecialityRepository,
    private val local: SpecialityLocalRepository,
) {
    suspend operator fun invoke(): Resource<List<Speciality>> {
        val cached = local.getSpecialities()
        if (cached.isNotEmpty()) return Resource.Success(cached.map { it.toDomain() })
        return when (val result = remote.getAllSpecialities()) {
            is Resource.Success -> {
                val entities = result.data?.data?.map { it.toDomain().toEntity() } ?: emptyList()
                local.saveSpecialities(entities)
                Resource.Success(entities.map { it.toDomain() })
            }
            is Resource.Error -> Resource.Error(result.message ?: "Failed to fetch specialities")
            is Resource.Loading -> Resource.Loading()
        }
    }
}
