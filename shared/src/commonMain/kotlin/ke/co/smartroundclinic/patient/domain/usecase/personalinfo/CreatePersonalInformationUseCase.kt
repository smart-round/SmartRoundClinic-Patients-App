package ke.co.smartroundclinic.patient.domain.usecase.personalinfo

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.PersonalInformation
import ke.co.smartroundclinic.patient.domain.repository.PersonalInformationRepository

class CreatePersonalInformationUseCase(private val repository: PersonalInformationRepository) {
    suspend operator fun invoke(
        gender: String,
        phoneNumber: String,
        countryCode: String,
        bloodGroup: String,
        dateOfBirth: String,
        weight: Double? = null,
        weightIn: String? = null,
        height: Double? = null,
        heightIn: String? = null,
        maritalStatus: String? = null,
        allergies: List<String> = emptyList(),
        chronicConditions: List<String> = emptyList(),
        currentMedications: List<String> = emptyList(),
    ): Resource<PersonalInformation?> =
        when (val result = repository.create(
            gender = gender,
            phoneNumber = phoneNumber,
            countryCode = countryCode,
            bloodGroup = bloodGroup,
            dateOfBirth = dateOfBirth,
            weight = weight,
            weightIn = weightIn,
            height = height,
            heightIn = heightIn,
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
