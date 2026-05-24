package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.ConsultationFileAttachment
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConsultationSession
import kotlinx.serialization.Serializable

// ─── Session ───────────────────────────────────────────────────────────────

@Serializable
data class ConsultationSessionResponse(
    val data: ConsultationSessionData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class ConsultationSessionData(
    val id: String,
    val appointmentId: String,
    val doctorId: String,
    val patientId: String,
    val status: String,
    val videoRoomId: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
)

fun ConsultationSessionData.toDomain() = ConsultationSession(
    id = id,
    appointmentId = appointmentId,
    doctorId = doctorId,
    patientId = patientId,
    status = status,
    videoRoomId = videoRoomId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// ─── Messages ──────────────────────────────────────────────────────────────

@Serializable
data class ConsultationMessagesResponse(
    val data: ConsultationMessagePageData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class ConsultationMessagePageData(
    val items: List<ConsultationMessageData> = emptyList(),
    val total: Long = 0,
    val page: Int = 1,
    val size: Int = 50,
)

@Serializable
data class ConsultationMessageData(
    val id: String,
    val consultationId: String,
    val senderId: String,
    val senderRole: String,
    val senderName: String,
    val messageType: String,
    val message: String? = null,
    val files: List<ConsultationFileData> = emptyList(),
    val createdAt: String,
    val updatedAt: String? = null,
)

@Serializable
data class ConsultationFileData(
    val fileName: String,
    val url: String,
    val contentType: String,
    val sizeBytes: Long,
)

fun ConsultationMessageData.toDomain() = ConsultationMessage(
    id = id,
    consultationId = consultationId,
    senderId = senderId,
    senderRole = senderRole,
    senderName = senderName,
    messageType = messageType,
    message = message,
    files = files.map { it.toDomain() },
    createdAt = createdAt,
)

fun ConsultationFileData.toDomain() = ConsultationFileAttachment(
    fileName = fileName,
    url = url,
    contentType = contentType,
    sizeBytes = sizeBytes,
)

// ─── Outgoing WS message ───────────────────────────────────────────────────

@Serializable
data class ConsultationWsOutgoing(
    val type: String,
    val message: String? = null,
    val fileName: String? = null,
    val contentType: String? = null,
    val data: String? = null,
)

@Serializable
data class ConsultationFileUploadResponse(
    val data: ConsultationMessageData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)
