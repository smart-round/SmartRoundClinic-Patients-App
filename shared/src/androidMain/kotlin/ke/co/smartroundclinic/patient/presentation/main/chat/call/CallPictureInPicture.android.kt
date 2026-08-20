package ke.co.smartroundclinic.patient.presentation.main.chat.call

import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ContextWrapper
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual object PipModeState {
    private val _isActive = mutableStateOf(false)
    actual val isActive: State<Boolean> = _isActive

    /** Set from `MainActivity.onPictureInPictureModeChanged()`. */
    fun set(active: Boolean) {
        _isActive.value = active
    }
}

@Composable
actual fun rememberMinimizeCallAction(isVideoEnabled: Boolean): () -> Unit {
    val activity = LocalContext.current.findActivityForPip()
        ?: error("rememberMinimizeCallAction requires an Activity host context")
    return remember(activity, isVideoEnabled) {
        {
            if (isVideoEnabled) activity.enterPictureInPictureMode(buildCallPipParams(activity))
            else activity.moveTaskToBack(true)
        }
    }
}

@Composable
actual fun CallBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

/**
 * Shared by [rememberPictureInPictureRequester] and `MainActivity.onUserLeaveHint()` — a 9:16
 * portrait aspect ratio (matches the call screen's full-bleed remote video) plus, on API 31+,
 * mute/end-call [RemoteAction]s so the collapsed PiP window is still operable without expanding
 * back to full screen.
 */
fun buildCallPipParams(activity: Activity): PictureInPictureParams {
    val builder = PictureInPictureParams.Builder().setAspectRatio(Rational(9, 16))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        builder.setActions(listOf(muteAction(activity), endCallAction(activity)))
    }
    return builder.build()
}

private fun muteAction(context: Context): RemoteAction {
    val muted = !ActiveCallSignal.isAudioEnabled.value
    val icon = Icon.createWithResource(
        context,
        if (muted) android.R.drawable.ic_lock_silent_mode else android.R.drawable.ic_lock_silent_mode_off,
    )
    val intent = Intent(context, CallPipActionReceiver::class.java).apply { action = CallPipActionReceiver.ACTION_TOGGLE_AUDIO }
    val pendingIntent = PendingIntent.getBroadcast(
        context, CallPipActionReceiver.REQUEST_TOGGLE_AUDIO, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    return RemoteAction(icon, "Mute", "Toggle microphone", pendingIntent)
}

private fun endCallAction(context: Context): RemoteAction {
    val icon = Icon.createWithResource(context, android.R.drawable.ic_menu_close_clear_cancel)
    val intent = Intent(context, CallPipActionReceiver::class.java).apply { action = CallPipActionReceiver.ACTION_END_CALL }
    val pendingIntent = PendingIntent.getBroadcast(
        context, CallPipActionReceiver.REQUEST_END_CALL, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    return RemoteAction(icon, "End call", "End call", pendingIntent)
}

/** Handles the PiP window's own mute/end-call [RemoteAction] taps — see [buildCallPipParams]. */
class CallPipActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TOGGLE_AUDIO -> ActiveCallSignal.onToggleAudio?.invoke()
            ACTION_END_CALL -> ActiveCallSignal.onEndCall?.invoke()
        }
    }

    companion object {
        const val ACTION_TOGGLE_AUDIO = "ke.co.smartroundclinic.patient.action.PIP_TOGGLE_AUDIO"
        const val ACTION_END_CALL = "ke.co.smartroundclinic.patient.action.PIP_END_CALL"
        const val REQUEST_TOGGLE_AUDIO = 9101
        const val REQUEST_END_CALL = 9102
    }
}

private tailrec fun Context.findActivityForPip(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivityForPip()
    else -> null
}
