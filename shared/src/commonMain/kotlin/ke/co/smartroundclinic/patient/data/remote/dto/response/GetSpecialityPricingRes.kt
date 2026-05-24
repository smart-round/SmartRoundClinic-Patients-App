package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.SpecialityPricing
import kotlinx.serialization.Serializable

@Serializable
data class GetSpecialityPricingRes(
    val `data`: GetSpecialityPricing,
    val httpStatusCode: Int, // 200
    val message: String, // Service tier retrieved successfully
    val status: Boolean // true
)

@Serializable
data class GetSpecialityPricing(
    val chatAccessWindow: Int, // 86400000
    val consultationDuration: Int, // 1500000
    val createdAt: String, // 2026-04-18T11:11:51.672864Z
    val followUpFee: Int, // 750
    val followUpWindow: Long, // 2592000000
    val gracePeriod: Int, // 600000
    val id: String, // 69e366f7bb4692c06d4a8e88
    val name: String, // Speciality
    val tierPrice: Double, // 1050.0
    val updatedAt: String? = null // 2026-04-18T11:14:13.137619Z
)

fun GetSpecialityPricing.toDomain() = SpecialityPricing(
    id = id,
    name = name,
    tierPrice = tierPrice,
    followUpFee = followUpFee,
    consultationDurationMs = consultationDuration,
    chatAccessWindowMs = chatAccessWindow,
)