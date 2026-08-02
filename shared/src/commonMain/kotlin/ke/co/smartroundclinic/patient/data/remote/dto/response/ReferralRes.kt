package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.Referral
import ke.co.smartroundclinic.patient.domain.model.ReferralStatus
import kotlinx.serialization.Serializable

@Serializable
data class GetPendingReferralsRes(
    val `data`: List<ReferralItem>?,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class ReferralActionRes(
    val `data`: ReferralItem?,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class ReferralItem(
    val id: String,
    val sourceAppointmentId: String,
    val referringDoctorId: String,
    val referringDoctorName: String? = null,
    val referringDoctorPicture: String? = null,
    val patientId: String,
    val patientName: String? = null,
    val receivingDoctorId: String,
    val receivingDoctorName: String? = null,
    val receivingDoctorPicture: String? = null,
    val reason: String,
    val status: String,
    val resultingAppointmentId: String? = null,
    val createdAt: String,
    val respondedAt: String? = null,
)

fun ReferralItem.toDomain() = Referral(
    id = id,
    sourceAppointmentId = sourceAppointmentId,
    referringDoctorId = referringDoctorId,
    referringDoctorName = referringDoctorName,
    referringDoctorPicture = referringDoctorPicture,
    receivingDoctorId = receivingDoctorId,
    receivingDoctorName = receivingDoctorName,
    receivingDoctorPicture = receivingDoctorPicture,
    reason = reason,
    status = ReferralStatus.from(status),
    createdAt = createdAt,
)
