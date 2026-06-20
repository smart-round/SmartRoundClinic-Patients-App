package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.PersonalInformation
import kotlinx.serialization.Serializable

@Serializable
data class PersonalInformationData(
    val weight: Double? = null,
    val weightIn: String? = null,
    val height: Double? = null,
    val heightIn: String? = null,
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
    weightIn = weightIn,
    height = height,
    heightIn = heightIn,
    bloodGroup = bloodGroup,
    maritalStatus = maritalStatus,
    allergies = allergies,
    chronicConditions = chronicConditions,
    currentMedications = currentMedications,
)
