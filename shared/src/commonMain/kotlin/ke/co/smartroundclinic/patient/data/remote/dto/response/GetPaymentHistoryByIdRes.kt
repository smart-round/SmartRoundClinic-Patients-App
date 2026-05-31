package ke.co.smartroundclinic.patient.data.remote.dto.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPaymentHistoryByIdRes(
    val `data`: GetPaymentHistoryByIdData,
    val httpStatusCode: Int, // 200
    val message: String, // Success
    val status: Boolean // true
)

@Serializable
data class GetPaymentHistoryByIdData(
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