package ke.co.smartroundclinic.patient.domain.model

data class ConsultationSession(
    val id: String,
    val appointmentId: String,
    val doctorId: String,
    val patientId: String,
    val status: String,
    val videoRoomId: String?,
    val createdAt: String,
    val updatedAt: String?,
)
