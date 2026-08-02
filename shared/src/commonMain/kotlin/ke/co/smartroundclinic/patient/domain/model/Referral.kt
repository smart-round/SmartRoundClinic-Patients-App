package ke.co.smartroundclinic.patient.domain.model

enum class ReferralStatus {
    PENDING, ACCEPTED, DECLINED, UNKNOWN;

    companion object {
        fun from(value: String): ReferralStatus = entries.firstOrNull { it.name == value.uppercase() } ?: UNKNOWN
    }
}

data class Referral(
    val id: String,
    val sourceAppointmentId: String,
    val referringDoctorId: String,
    val referringDoctorName: String?,
    val referringDoctorPicture: String?,
    val receivingDoctorId: String,
    val receivingDoctorName: String?,
    val receivingDoctorPicture: String?,
    val reason: String,
    val status: ReferralStatus,
    val createdAt: String,
)
