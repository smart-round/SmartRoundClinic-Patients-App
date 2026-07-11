package ke.co.smartroundclinic.patient.domain.model

/** Result of ringing the other party of a thread (see InviteToCallUseCase) — before either side has joined the meeting. */
data class CallInvite(
    val callId: String,
    val ringTimeoutSeconds: Long,
)

/** Ringing state for an incoming call, sourced from either a push payload or the chat WebSocket. */
data class IncomingCall(
    val callId: String,
    val callerId: String,
    val callerName: String?,
    val doctorId: String,
    val patientId: String,
    val isVideo: Boolean,
    val ringTimeoutSeconds: Long,
)
