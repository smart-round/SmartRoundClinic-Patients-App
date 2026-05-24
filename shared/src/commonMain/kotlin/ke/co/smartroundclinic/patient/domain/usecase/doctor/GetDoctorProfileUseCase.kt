package ke.co.smartroundclinic.patient.domain.usecase.doctor

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.DoctorProfile
import ke.co.smartroundclinic.patient.domain.repository.DoctorRepository

class GetDoctorProfileUseCase(private val repository: DoctorRepository) {
    suspend operator fun invoke(doctorId: String): Resource<DoctorProfile> =
        when (val result = repository.getDoctorProfile(doctorId)) {
            is Resource.Success -> result.data?.data?.toDomain()
                ?.let { Resource.Success(it) }
                ?: Resource.Error("No profile data returned")
            is Resource.Error -> Resource.Error(result.message ?: "Failed to fetch doctor profile")
            is Resource.Loading -> Resource.Loading()
        }
}
