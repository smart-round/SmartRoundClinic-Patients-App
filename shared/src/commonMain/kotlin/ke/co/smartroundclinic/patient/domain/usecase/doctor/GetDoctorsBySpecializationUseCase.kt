package ke.co.smartroundclinic.patient.domain.usecase.doctor

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.domain.repository.DoctorRepository

class GetDoctorsBySpecializationUseCase(private val repository: DoctorRepository) {
    suspend operator fun invoke(
        specializationId: String,
        page: Int = 1,
        size: Int = 50,
    ): Resource<List<Doctor>> =
        when (val result = repository.getDoctorBySpecialization(specializationId, page, size)) {
            is Resource.Success -> Resource.Success(
                result.data?.data?.items?.map { it.toDomain() } ?: emptyList(),
            )
            is Resource.Error -> Resource.Error(result.message ?: "Failed to fetch doctors")
            is Resource.Loading -> Resource.Loading()
        }
}
