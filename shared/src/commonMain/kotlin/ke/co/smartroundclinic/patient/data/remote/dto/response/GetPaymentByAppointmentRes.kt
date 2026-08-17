package ke.co.smartroundclinic.patient.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class GetPaymentByAppointmentRes(
    val `data`: PaymentByAppointmentData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class PaymentByAppointmentData(
    val id: String,
    val amount: Double,
    val currency: String,
    val status: String,
)
