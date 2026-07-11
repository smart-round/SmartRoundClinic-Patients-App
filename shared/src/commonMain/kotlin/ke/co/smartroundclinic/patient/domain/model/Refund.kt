package ke.co.smartroundclinic.patient.domain.model

data class Refund(
    val id: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val reason: String?,
    val createdAt: String,
    val updatedAt: String?,
)
