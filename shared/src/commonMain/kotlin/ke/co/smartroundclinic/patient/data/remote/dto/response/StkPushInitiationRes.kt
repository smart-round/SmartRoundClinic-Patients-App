package ke.co.smartroundclinic.patient.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class StkPushInitiationRes(
    val httpStatusCode: Int,
    val status: Boolean,
    val message: String,
    val data: StkPushInitiationData?,
)

@Serializable
data class StkPushInitiationData(
    val invoiceId: String,
    val transactionRef: String,
    val state: String,
    val amount: Double,
    val currency: String,
)
