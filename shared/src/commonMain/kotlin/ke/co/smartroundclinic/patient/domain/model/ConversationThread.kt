package ke.co.smartroundclinic.patient.domain.model

/** What the thread's most recent message was, so the list can show a matching icon. */
enum class ThreadPreviewKind { TEXT, PHOTO, VIDEO, FILE, PRESCRIPTION }

/** One conversation per (doctorId, patientId) pair — merges all of that pair's consultations. */
data class ConversationThread(
    val threadId: String,
    val doctorId: String,
    val patientId: String,
    val counterpartName: String,
    val counterpartPicture: String?,
    val lastMessagePreview: String?,
    val lastMessageAt: String?,
    val latestAppointmentId: String,
    val isOnline: Boolean = false,
    val lastSeenAt: String? = null,
    val lastMessageKind: ThreadPreviewKind = ThreadPreviewKind.TEXT,
)
