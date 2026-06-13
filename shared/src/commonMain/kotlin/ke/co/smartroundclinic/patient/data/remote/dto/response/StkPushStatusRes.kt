package ke.co.smartroundclinic.patient.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class StkPushStatusRes(
    val httpStatusCode: Int,
    val status: Boolean,
    val message: String,
    val data: StkPushStatusData?,
)

@Serializable
data class StkPushStatusData(
    val invoice: StkPushStatusInvoice,
    val meta: StkPushStatusMeta,
)

@Serializable
data class StkPushStatusInvoice(
    val invoiceId: String,
    val state: String,
    val failedReason: String?,
    val value: Double,
    val currency: String,
)

@Serializable
data class StkPushStatusMeta(
    val id: String,
)
