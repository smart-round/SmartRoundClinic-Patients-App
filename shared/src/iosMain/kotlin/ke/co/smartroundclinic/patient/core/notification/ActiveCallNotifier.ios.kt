package ke.co.smartroundclinic.patient.core.notification

actual object ActiveCallNotifier {
    actual fun notifyCallEnded() {
        CallKitBridge.onEndActiveCall?.invoke()
    }
}
