package ke.co.smartroundclinic.patient.core.notification

/**
 * Platform reaction to a call-signaling push arriving while the app has no foreground UI open
 * to react to it directly (see IncomingCallState for the foreground/WebSocket path). Android
 * shows a full-screen incoming-call notification; iOS's reliable channel is native
 * CallKit/PushKit, which bypasses this Kotlin callback chain entirely for backgrounded/killed
 * apps — the iOS actual only covers the case where the app process happens to still be alive.
 */
expect object IncomingCallHandler {
    fun onCallInvite(
        callId: String,
        callerId: String,
        callerName: String?,
        doctorId: String,
        patientId: String,
        isVideo: Boolean,
        ringTimeoutSeconds: Long,
    )
    fun onCallAnswered(callId: String)
    fun onCallDeclined(callId: String)
    fun onCallCancelled(callId: String)
}
