package ke.co.smartroundclinic.patient.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateTicketReq(
    val issueCategoryId: String,
    val title: String,
    val description: String,
    val complainantName: String,
    val complainantEmail: String,
)

@Serializable
data class WsChatMessageReq(
    val message: String,
)
