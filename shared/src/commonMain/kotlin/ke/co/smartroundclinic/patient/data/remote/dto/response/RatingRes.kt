package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.Rating
import kotlinx.serialization.Serializable

@Serializable
data class RatingData(
    val id: String,
    val appointmentId: String,
    val doctorId: String,
    val patientId: String,
    val rating: Int,
    val comment: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val patientName: String? = null,
    val patientProfilePicture: String? = null,
)

@Serializable
data class RatingResponse(
    val data: RatingData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class RatingListData(
    val items: List<RatingData> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val size: Int = 20,
)

@Serializable
data class RatingListResponse(
    val data: RatingListData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

fun RatingData.toDomain() = Rating(
    id = id,
    appointmentId = appointmentId,
    doctorId = doctorId,
    patientId = patientId,
    rating = rating,
    comment = comment,
    createdAt = createdAt,
    updatedAt = updatedAt,
    raterName = patientName,
    raterProfilePicture = patientProfilePicture,
)
