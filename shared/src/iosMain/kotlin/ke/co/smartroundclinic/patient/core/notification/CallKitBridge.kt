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

    /** Ends whichever call CallKit currently has active (answered), if any — see
     * CallKitManager.endActiveCall(). Invoked via [ActiveCallNotifier] once the in-app Call
     * screen ends, so CallKit's system call state (status bar indicator, Recents, Dynamic
     * Island) doesn't linger after our own UI has already moved on. */
    var onEndActiveCall: (() -> Unit)? = null
}
