package ke.co.smartroundclinic.patient.domain.usecase.doctor

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.database.entity.toDomain
import ke.co.smartroundclinic.patient.core.database.entity.toEntity
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.domain.repository.DoctorLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.DoctorRepository

class GetRecommendedDoctorsUseCase(
    private val remote: DoctorRepository,
    private val local: DoctorLocalRepository,
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Resource<List<Doctor>> {
        val cached = local.getDoctors()
        if (!forceRefresh && cached.isNotEmpty()) return Resource.Success(cached.map { it.toDomain() })
        return when (val result = remote.getRecommendedDoctors()) {
            is Resource.Success -> {
                val entities = result.data?.data?.items?.map { it.toDomain().toEntity() } ?: emptyList()
                local.saveDoctors(entities)
                Resource.Success(entities.map { it.toDomain() })
            }
            is Resource.Error -> Resource.Error(result.message ?: "Failed to fetch doctors")
            is Resource.Loading -> Resource.Loading()
        }
    }
}
