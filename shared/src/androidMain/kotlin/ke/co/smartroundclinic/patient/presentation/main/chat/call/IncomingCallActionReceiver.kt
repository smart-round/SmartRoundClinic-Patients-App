package ke.co.smartroundclinic.patient.presentation.main.chat.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import ke.co.smartroundclinic.patient.core.notification.CallActionDispatcher
import ke.co.smartroundclinic.patient.core.notification.IncomingCallState

private const val CALL_NOTIFICATION_ID = 9001

/**
 * Handles the notification tray's Decline action button — a fallback path for devices where
 * the full-screen-intent notification only shows as a plain heads-up banner instead of
 * launching IncomingCallActivity (Android 14+ treats USE_FULL_SCREEN_INTENT as a special
 * permission that isn't granted by default; see MainActivity's requestFullScreenIntentPermission).
 * Declining from here never needs to open the app UI at all.
 */
class IncomingCallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: return
        val callerId = intent.getStringExtra(EXTRA_CALLER_ID) ?: return

        NotificationManagerCompat.from(context).cancel(CALL_NOTIFICATION_ID)
        IncomingCallState.clear(callId)
        CallActionDispatcher.decline(callerId, callId)
    }

    companion object {
        const val ACTION_DECLINE = "ke.co.smartroundclinic.patient.action.DECLINE_CALL"
        const val EXTRA_CALL_ID = "callId"
        const val EXTRA_CALLER_ID = "callerId"
    }
}
