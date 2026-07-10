package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.ConsultationFileAttachment
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConsultationSession
import ke.co.smartroundclinic.patient.domain.model.ConversationThread
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
    val isTyping: Boolean? = null,
)

/**
 * Minimal peek shape decoded first from every incoming WS frame to dispatch — TEXT/FILE/PRESCRIPTION
 * (or an absent/unrecognized type) fall through to [ConsultationMessageData]; TYPING/PRESENCE/READ
 * decode as their own small shapes below. Never constructed directly, only decoded into.
 */
@Serializable
data class ConsultationWsEventPeek(val type: String? = null)

@Serializable
data class ConsultationTypingEventData(
    val type: String = "TYPING",
    val senderId: String,
    val isTyping: Boolean,
)

@Serializable
data class ConsultationPresenceEventData(
    val type: String = "PRESENCE",
    val userId: String,
    val isOnline: Boolean,
    val lastSeenAt: String? = null,
)

@Serializable
data class ConsultationReadReceiptEventData(
    val type: String = "READ",
    val doctorId: String,
    val patientId: String,
    val readerId: String,
    val lastReadAt: String,
)

@Serializable
data class ConsultationFileUploadResponse(
    val data: ConsultationMessageData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class JoinCallData(
    val meetingId: String,
    val participantId: String,
    val authToken: String,
    val presetName: String,
)

@Serializable
data class JoinCallResponse(
    val data: JoinCallData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

fun JoinCallData.toDomain() = ke.co.smartroundclinic.patient.domain.model.CallJoinInfo(
    meetingId = meetingId,
    participantId = participantId,
    authToken = authToken,
    presetName = presetName,
)

// ─── Conversation threads ───────────────────────────────────────────────────

@Serializable
data class ConversationThreadsResponse(
    val data: List<ConversationThreadData>? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class ConversationThreadData(
    val threadId: String,
    val doctorId: String,
    val patientId: String,
    val counterpartName: String,
    val counterpartPicture: String? = null,
    val lastMessagePreview: String? = null,
    val lastMessageAt: String? = null,
    val latestConsultationStatus: String,
    val latestAppointmentId: String,
    val isOnline: Boolean = false,
    val lastSeenAt: String? = null,
)

fun ConversationThreadData.toDomain() = ConversationThread(
    threadId = threadId,
    doctorId = doctorId,
    patientId = patientId,
    counterpartName = counterpartName,
    counterpartPicture = counterpartPicture,
    lastMessagePreview = lastMessagePreview,
    lastMessageAt = lastMessageAt,
    latestConsultationStatus = latestConsultationStatus,
    latestAppointmentId = latestAppointmentId,
    isOnline = isOnline,
    lastSeenAt = lastSeenAt,
)

@Serializable
data class ConversationThreadMessagesResponse(
    val data: ConversationThreadMessagesData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class ConversationThreadMessagesData(
    val items: List<ConsultationMessageData> = emptyList(),
    val nextCursor: String? = null,
    val counterpartLastReadAt: String? = null,
    val counterpartLastDeliveredAt: String? = null,
)
