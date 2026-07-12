package ke.co.smartroundclinic.patient.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import ke.co.smartroundclinic.patient.domain.model.IncomingCall
import ke.co.smartroundclinic.patient.presentation.main.chat.call.IncomingCallActionReceiver
import ke.co.smartroundclinic.patient.presentation.main.chat.call.IncomingCallActivity
import org.koin.core.context.GlobalContext

private const val CALL_CHANNEL_ID = "incoming_calls"
private const val CALL_NOTIFICATION_ID = 9001

/**
 * Android's reliable incoming-call path: a full-screen-intent notification on a dedicated
 * high-importance channel, which the OS is allowed to auto-launch [IncomingCallActivity] even
 * over the lock screen — Android's FCM delivery guarantees `onMessageReceived` (and therefore
 * this) run for data-only messages whether the app is foregrounded, backgrounded, or killed.
 */
actual object IncomingCallHandler {
    private fun context(): Context = GlobalContext.get().get()

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CALL_CHANNEL_ID) != null) return
        val channel = NotificationChannel(CALL_CHANNEL_ID, "Incoming Calls", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Incoming video/voice call invitations"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

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

        val context = context()
        ensureChannel(context)

        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(IncomingCallActivity.EXTRA_CALL_ID, callId)
            putExtra(IncomingCallActivity.EXTRA_CALLER_ID, callerId)
            putExtra(IncomingCallActivity.EXTRA_CALLER_NAME, callerName)
            putExtra(IncomingCallActivity.EXTRA_DOCTOR_ID, doctorId)
            putExtra(IncomingCallActivity.EXTRA_PATIENT_ID, patientId)
            putExtra(IncomingCallActivity.EXTRA_IS_VIDEO, isVideo)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            callId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Fallback action buttons — Android 14+ treats USE_FULL_SCREEN_INTENT as a special
        // permission the user must separately grant (see MainActivity), so setFullScreenIntent
        // silently downgrades to a plain heads-up banner unless it's been granted. These give a
        // working Answer/Decline either way, not just when the full-screen launch succeeds.
        val answerIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(IncomingCallActivity.EXTRA_CALL_ID, callId)
            putExtra(IncomingCallActivity.EXTRA_CALLER_ID, callerId)
            putExtra(IncomingCallActivity.EXTRA_CALLER_NAME, callerName)
            putExtra(IncomingCallActivity.EXTRA_DOCTOR_ID, doctorId)
            putExtra(IncomingCallActivity.EXTRA_PATIENT_ID, patientId)
            putExtra(IncomingCallActivity.EXTRA_IS_VIDEO, isVideo)
            putExtra(IncomingCallActivity.EXTRA_AUTO_ACCEPT, true)
        }
        val answerPendingIntent = PendingIntent.getActivity(
            context,
            (callId + "_answer").hashCode(),
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val declineIntent = Intent(context, IncomingCallActionReceiver::class.java).apply {
            action = IncomingCallActionReceiver.ACTION_DECLINE
            putExtra(IncomingCallActionReceiver.EXTRA_CALL_ID, callId)
            putExtra(IncomingCallActionReceiver.EXTRA_CALLER_ID, callerId)
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            (callId + "_decline").hashCode(),
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // CallStyle renders the system's native incoming-call layout — filled green Answer /
        // red Decline buttons — instead of the plain text buttons a normal addAction() gives.
        val caller = Person.Builder().setName(callerName ?: "Doctor").setImportant(true).build()
        val callStyle = NotificationCompat.CallStyle.forIncomingCall(caller, declinePendingIntent, answerPendingIntent)

        val notification = NotificationCompat.Builder(context, CALL_CHANNEL_ID)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(callerName ?: "Doctor")
            .setContentText(if (isVideo) "Incoming video call" else "Incoming call")
            .setStyle(callStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(answerPendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        NotificationManagerCompat.from(context).notify(CALL_NOTIFICATION_ID, notification)
    }

    // Received by the CALLER — the callee just answered, so this device (which placed the
    // call) should now join too. No ring UI to dismiss here; see OutgoingCallState.
    actual fun onCallAnswered(callId: String) {
        OutgoingCallState.answered(callId)
    }

    // Received by the CALLER — the callee declined.
    actual fun onCallDeclined(callId: String) {
        OutgoingCallState.declined(callId)
    }

    // Received by the CALLEE — the caller hung up (or the client-side ring timer expired)
    // before this device answered. Dismiss the ringing notification/activity.
    actual fun onCallCancelled(callId: String) {
        IncomingCallState.clear(callId)
        NotificationManagerCompat.from(context()).cancel(CALL_NOTIFICATION_ID)
    }
}
