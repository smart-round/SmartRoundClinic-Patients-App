package ke.co.smartroundclinic.patient.domain.model

data class ConsultationMessage(
    val id: String,
    val consultationId: String,
    val senderId: String,
    val senderRole: String,
    val senderName: String,
    val messageType: String,
    val message: String?,
    val files: List<ConsultationFileAttachment>,
    val createdAt: String,
)

data class ConsultationFileAttachment(
    val fileName: String,
    val url: String,
    val contentType: String,
    val sizeBytes: Long,
)
