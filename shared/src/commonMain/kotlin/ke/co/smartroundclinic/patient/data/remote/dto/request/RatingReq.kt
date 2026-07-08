package ke.co.smartroundclinic.patient.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SubmitDoctorRatingReq(
    val appointmentId: String,
    val doctorId: String,
    val rating: Int,
    val comment: String? = null,
)

@Serializable
data class UpdateRatingReq(
    val rating: Int? = null,
    val comment: String? = null,
)
