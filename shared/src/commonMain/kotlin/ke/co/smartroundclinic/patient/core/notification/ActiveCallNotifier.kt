package ke.co.smartroundclinic.patient.core.notification

/**
 * Tells the platform's native call UI that the in-app Call screen has ended, so it can drop any
 * system-level call state it's still holding onto. iOS's CallKit is the only platform that needs
 * this (see CallKitBridge.onEndActiveCall / CallKitManager.endActiveCall()) — Android's
 * IncomingCallActivity already finishes itself as soon as the call is accepted, before the Call
 * screen even opens, so there's nothing left to dismiss there.
 */
expect object ActiveCallNotifier {
    fun notifyCallEnded()
}
