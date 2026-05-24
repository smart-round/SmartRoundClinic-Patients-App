package ke.co.smartroundclinic.patient.domain.model

data class SpecialityPricing(
    val id: String,
    val name: String,
    val tierPrice: Double,
    val followUpFee: Int,
    val consultationDurationMs: Int,
    val chatAccessWindowMs: Int,
)
