package ke.co.smartroundclinic.patient.domain.usecase.personalinfo

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.PersonalInformation
import ke.co.smartroundclinic.patient.domain.repository.PersonalInformationRepository

class GetPersonalInformationUseCase(private val repository: PersonalInformationRepository) {
    suspend operator fun invoke(): Resource<PersonalInformation?> =
        when (val result = repository.get()) {
            is Resource.Success -> Resource.Success(
                data = result.data?.data?.toDomain(),
                message = result.message ?: "Success",
            )
            is Resource.Error -> Resource.Error(result.message ?: "An unknown error occurred")
            is Resource.Loading -> Resource.Loading()
        }
}
