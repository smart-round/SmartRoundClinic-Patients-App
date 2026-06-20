package ke.co.smartroundclinic.patient.domain.usecase.personalinfo

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.PersonalInformation
import ke.co.smartroundclinic.patient.domain.repository.PersonalInformationRepository

class UpdatePersonalInformationUseCase(private val repository: PersonalInformationRepository) {
    suspend operator fun invoke(
        weight: Double? = null,
        weightUnit: String? = null,
        height: Double? = null,
        heightUnit: String? = null,
        bloodGroup: String? = null,
        maritalStatus: String? = null,
        allergies: List<String>? = null,
        chronicConditions: List<String>? = null,
        currentMedications: List<String>? = null,
    ): Resource<PersonalInformation?> =
        when (val result = repository.update(
            weight = weight,
            weightUnit = weightUnit,
            height = height,
            heightUnit = heightUnit,
            bloodGroup = bloodGroup,
            maritalStatus = maritalStatus,
            allergies = allergies,
            chronicConditions = chronicConditions,
            currentMedications = currentMedications,
        )) {
            is Resource.Success -> Resource.Success(
                data = result.data?.data?.toDomain(),
                message = result.message ?: "Success",
            )
            is Resource.Error -> Resource.Error(result.message ?: "An unknown error occurred")
            is Resource.Loading -> Resource.Loading()
        }
}
