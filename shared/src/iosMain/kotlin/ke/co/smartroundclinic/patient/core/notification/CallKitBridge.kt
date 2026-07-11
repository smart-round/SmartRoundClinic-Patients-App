package ke.co.smartroundclinic.patient.core.notification

/**
 * Kotlin -> Swift bridge for reporting/ending calls via CallKit (same shape as
 * `RtkCallBridge` for RealtimeKit) — Swift sets these once at app startup, see
 * `iOSApp.swift`'s `wireCallKitBridge()`. If unset (e.g. VoIP push not configured yet),
 * [IncomingCallHandler] falls back to just updating [IncomingCallState].
 */
object CallKitBridge {
    var onIncomingCall: ((callId: String, callerName: String?, isVideo: Boolean) -> Unit)? = null
    var onEndCall: ((callId: String) -> Unit)? = null
}
