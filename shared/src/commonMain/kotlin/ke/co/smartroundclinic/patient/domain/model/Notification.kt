package ke.co.smartroundclinic.patient.domain.model

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val channel: String,
    val status: String,
    val createdAt: String,
    val readAt: String?,
) {
    val isUnread: Boolean get() = status.equals("UNREAD", ignoreCase = true)
}
