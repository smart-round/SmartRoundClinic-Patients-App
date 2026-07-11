package ke.co.smartroundclinic.patient.core.notification

import ke.co.smartroundclinic.patient.domain.model.IncomingCall

/**
 * iOS's reliable incoming-call path is a genuine VoIP push delivered via PushKit, which
 * `iOSApp.swift` reports to CXProvider through [CallKitBridge] without going through this
 * Kotlin callback at all — that's the only channel that reliably wakes a backgrounded/killed
 * app. This actual covers the FCM silent-push fallback and the foreground WebSocket path:
 * it updates the shared ringing state (for any in-app UI) and, if the app happens to be alive
 * when this fires, also reports to CallKit via the bridge so the system call UI stays
 * consistent regardless of which channel the signal arrived on.
 */
actual object IncomingCallHandler {
    actual fun onCallInvite(
        callId: String,
        callerId: String,
        callerName: String?,
        doctorId: String,
        patientId: String,
        isVideo: Boolean,
        ringTimeoutSeconds: Long,
    ) {
        IncomingCallState.ring(
            IncomingCall(
                callId = callId,
                callerId = callerId,
                callerName = callerName,
                doctorId = doctorId,
                patientId = patientId,
                isVideo = isVideo,
                ringTimeoutSeconds = ringTimeoutSeconds,
            )
        )
        CallKitBridge.onIncomingCall?.invoke(callId, callerName, isVideo)
    }

    // Received by the CALLER — the callee just answered, so this device (which placed the
    // call) should now join too. No CallKit session to end here; see OutgoingCallState.
    actual fun onCallAnswered(callId: String) {
        OutgoingCallState.answered(callId)
    }

    // Received by the CALLER — the callee declined.
    actual fun onCallDeclined(callId: String) {
        OutgoingCallState.declined(callId)
    }

    // Received by the CALLEE — the caller hung up (or the client-side ring timer expired)
    // before this device answered. Ends the CallKit incoming-call session.
    actual fun onCallCancelled(callId: String) {
        IncomingCallState.clear(callId)
        CallKitBridge.onEndCall?.invoke(callId)
    }
}
