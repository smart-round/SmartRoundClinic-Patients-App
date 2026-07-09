package ke.co.smartroundclinic.patient.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreatePersonalInformationReq(
    val gender: String,
    val phoneNumber: String,
    val countryCode: String,
    val bloodGroup: String,
    val dateOfBirth: String,
    val weight: Double? = null,
    val weightIn: String? = null,
    val height: Double? = null,
    val heightIn: String? = null,
    val maritalStatus: String? = null,
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val currentMedications: List<String> = emptyList(),
)
