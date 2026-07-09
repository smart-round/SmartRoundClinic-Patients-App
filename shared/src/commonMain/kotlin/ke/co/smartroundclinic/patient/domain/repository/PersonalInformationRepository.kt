package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.PersonalInformationResponse

interface PersonalInformationRepository {
    suspend fun get(): Resource<PersonalInformationResponse>
    suspend fun create(
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
    ): Resource<PersonalInformationResponse>
    suspend fun update(
        weight: Double? = null,
        weightIn: String? = null,
        height: Double? = null,
        heightIn: String? = null,
        bloodGroup: String? = null,
        maritalStatus: String? = null,
        allergies: List<String>? = null,
        chronicConditions: List<String>? = null,
        currentMedications: List<String>? = null,
    ): Resource<PersonalInformationResponse>
}
