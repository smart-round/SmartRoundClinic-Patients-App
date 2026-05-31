package ke.co.smartroundclinic.patient.data.remote.dto.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PreBookAppointmentRes(
    val `data`: PreBookAppointmentData,
    val httpStatusCode: Int, // 201
    val message: String, // Payment link created successfully
    val status: Boolean // true
)

@Serializable
data class PreBookAppointmentData(
    val amount: Int, // 120
    val cardTarrif: String, // CUSTOMER-PAYS
    val createdAt: String, // 2026-05-31T18:08:35.106839+03:00
    val currency: String, // KES
    val id: String, // e8bf51f4-c998-404a-92ca-bbd5a5bfe862
    val isActive: Boolean, // true
    val mobileTarrif: String, // CUSTOMER-PAYS
    val qrcodeFile: String, // https://intasend-staging.s3.amazonaws.com/qrcodes/e8bf51f4-c998-404a-92ca-bbd5a5bfe862-34971045-ae82-4f53-8040-03935ee98ce0.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIA5PEU6MDIXYQ5527D%2F20260531%2Feu-central-1%2Fs3%2Faws4_request&X-Amz-Date=20260531T150835Z&X-Amz-Expires=600&X-Amz-SignedHeaders=host&X-Amz-Signature=9e53c6386618e4727f0a6f772dcf3d7f481805eb2daa9bf8ebfdfbdfb154e29c
    val redirectUrl: String, // https://vk5k99.instatunnel.my/payments/intasend/callback
    val title: String, // Consultation Payment 2026-05-31 18-08
    val updatedAt: String, // 2026-05-31T18:08:35.427277+03:00
    val url: String, // https://sandbox.intasend.com/pay/e8bf51f4-c998-404a-92ca-bbd5a5bfe862/
    val usageLimit: Int // 1
)