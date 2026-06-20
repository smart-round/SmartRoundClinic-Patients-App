package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.PersonalInformation
import kotlinx.serialization.Serializable

@Serializable
data class PersonalInformationData(
    val weight: Double? = null,
    val weightUnit: String? = null,
    val height: Double? = null,
    val heightUnit: String? = null,
    val bloodGroup: String? = null,
    val maritalStatus: String? = null,
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val currentMedications: List<String> = emptyList(),
)

@Serializable
data class PersonalInformationResponse(
    val data: PersonalInformationData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

fun PersonalInformationData.toDomain() = PersonalInformation(
    weight = weight,
    weightUnit = weightUnit,
    height = height,
    heightUnit = heightUnit,
    bloodGroup = bloodGroup,
    maritalStatus = maritalStatus,
    allergies = allergies,
    chronicConditions = chronicConditions,
    currentMedications = currentMedications,
)
