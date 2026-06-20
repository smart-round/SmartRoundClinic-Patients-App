package ke.co.smartroundclinic.patient.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePersonalInformationReq(
    val weight: Double? = null,
    val weightUnit: String? = null,
    val height: Double? = null,
    val heightUnit: String? = null,
    val bloodGroup: String? = null,
    val maritalStatus: String? = null,
    val allergies: List<String>? = null,
    val chronicConditions: List<String>? = null,
    val currentMedications: List<String>? = null,
)
