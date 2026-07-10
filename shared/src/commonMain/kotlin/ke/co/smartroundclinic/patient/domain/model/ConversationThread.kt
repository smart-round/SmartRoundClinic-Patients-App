package ke.co.smartroundclinic.patient.domain.model

/** One conversation per (doctorId, patientId) pair — merges all of that pair's consultations. */
data class ConversationThread(
    val threadId: String,
    val doctorId: String,
    val patientId: String,
    val counterpartName: String,
    val counterpartPicture: String?,
    val lastMessagePreview: String?,
    val lastMessageAt: String?,
    val latestConsultationStatus: String,
    val latestAppointmentId: String,
    val isOnline: Boolean = false,
    val lastSeenAt: String? = null,
)
