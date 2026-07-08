package ke.co.smartroundclinic.patient.domain.model

data class Rating(
    val id: String,
    val appointmentId: String,
    val doctorId: String,
    val patientId: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String,
    val updatedAt: String?,
    val raterName: String? = null,
    val raterProfilePicture: String? = null,
)
