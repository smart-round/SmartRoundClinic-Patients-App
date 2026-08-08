package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.ConsultationFileAttachment
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConversationThread
import ke.co.smartroundclinic.patient.domain.model.ThreadPreviewKind
import kotlinx.serialization.Serializable

// ─── Messages ──────────────────────────────────────────────────────────────

@Serializable
data class ConsultationMessageData(
    val id: String,
    val senderId: String,
    val senderRole: String,
    val senderName: String,
    val messageType: String,
    val message: String? = null,
    val files: List<ConsultationFileData> = emptyList(),
    val appointmentId: String? = null,
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
    senderId = senderId,
    senderRole = senderRole,
    senderName = senderName,
    messageType = messageType,
    message = message,
    files = files.map { it.toDomain() },
    appointmentId = appointmentId,
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

// Ringing-call signals, delivered over this same socket alongside push (see InviteToCallUseCase
// on the backend) — the socket is the low-latency path while either party has the thread open;
// push is the fallback for backgrounded/killed apps.
@Serializable
data class ConsultationCallInviteEventData(
    val type: String = "CALL_INVITE",
    val callId: String,
    val callerId: String,
    val callerName: String? = null,
    val isVideo: Boolean,
    val ringTimeoutSeconds: Long,
)

@Serializable
data class ConsultationCallAnsweredEventData(
    val type: String = "CALL_ANSWERED",
    val callId: String,
)

@Serializable
data class ConsultationCallDeclinedEventData(
    val type: String = "CALL_DECLINED",
    val callId: String,
)

@Serializable
data class ConsultationCallCancelledEventData(
    val type: String = "CALL_CANCELLED",
    val callId: String,
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
    val latestAppointmentId: String,
    val isOnline: Boolean = false,
    val lastSeenAt: String? = null,
    /** Absent on older API builds — defaults to TEXT, i.e. show the preview with no icon. */
    val lastMessageKind: String? = null,
)

fun ConversationThreadData.toDomain() = ConversationThread(
    threadId = threadId,
    doctorId = doctorId,
    patientId = patientId,
    counterpartName = counterpartName,
    counterpartPicture = counterpartPicture,
    lastMessagePreview = lastMessagePreview,
    lastMessageAt = lastMessageAt,
    latestAppointmentId = latestAppointmentId,
    isOnline = isOnline,
    lastSeenAt = lastSeenAt,
    lastMessageKind = when (lastMessageKind?.uppercase()) {
        "PHOTO" -> ThreadPreviewKind.PHOTO
        "VIDEO" -> ThreadPreviewKind.VIDEO
        "FILE" -> ThreadPreviewKind.FILE
        "PRESCRIPTION" -> ThreadPreviewKind.PRESCRIPTION
        else -> ThreadPreviewKind.TEXT
    },
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
)

// ─── Pre-signed direct-to-storage upload ────────────────────────────────────

@Serializable
data class PresignUploadReq(
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
)

@Serializable
data class PresignUploadData(
    val messageId: String,
    val key: String,
    val uploadUrl: String,
    val contentType: String,
)

@Serializable
data class PresignUploadResponse(
    val data: PresignUploadData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class CompleteUploadReq(
    val messageId: String,
    val key: String,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
)
