package ke.co.smartroundclinic.patient.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AvailabilityRes(
    val data: List<String> = emptyList(),
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)
