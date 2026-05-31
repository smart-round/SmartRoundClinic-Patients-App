package ke.co.smartroundclinic.patient.data.remote.dto.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPaymentHistoryRes(
    val `data`: GetPaymentHistoryData,
    val httpStatusCode: Int, // 200
    val message: String, // Success
    val status: Boolean // true
)

@Serializable
data class GetPaymentHistoryData(
    val items: List<GetPaymentHistoryItem>,
    val page: Int, // 3
    val pages: Int, // 3
    val size: Int, // 1
    val total: Int // 3
)

@Serializable
data class GetPaymentHistoryItem(
    val id: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val paymentMethod: String,
    val transactionRef: String,
    val commissionRate: Double,
    val platformFee: Double,
    val netEarnings: Double,
    val createdAt: String,
    val doctorId: String,
    val patientId: String,
    // Only present once payment is completed
    val account: String? = null,
    val appointmentId: String? = null,
    val charges: String? = null,
    val invoiceId: String? = null,
    val mpesaReference: String? = null,
    val netAmount: String? = null,
    val updatedAt: String? = null,
)