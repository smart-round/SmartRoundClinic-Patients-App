package ke.co.smartroundclinic.patient.presentation.main.chat.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cloudflare.realtimekit.models.RtkMeetingInfo
import com.cloudflare.realtimekit.ui.RealtimeKitUIBuilder
import com.cloudflare.realtimekit.ui.RealtimeKitUIInfo
import io.github.aakira.napier.Napier

/**
 * Android actual — launches the Cloudflare RealtimeKit UI Kit `MeetingActivity`
 * via `RealtimeKitUIBuilder.build(...).startMeeting()` and reports `onLeave`
 * back to Compose when the participant exits.
 *
 * Requires the calling host activity to extend [FragmentActivity] (which
 * `androidx.activity.ComponentActivity` does in androidx.activity 1.7+).
 */
@Composable
actual fun RealtimeMeetingView(
    authToken: String,
    enableVideo: Boolean,
    onLeave: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() as? FragmentActivity }
    val currentOnLeave by rememberUpdatedState(onLeave)
    var launched by remember(authToken) { mutableStateOf(false) }
    var hostPaused by remember { mutableStateOf(false) }

    LaunchedEffect(authToken, activity) {
        if (activity == null) {
            Napier.e(tag = "RealtimeKit", message = "Host activity is not a FragmentActivity — cannot launch meeting. Update MainActivity to extend FragmentActivity.")
            return@LaunchedEffect
        }
        if (launched) return@LaunchedEffect
        launched = true

        try {
            Napier.i(tag = "RealtimeKit", message = "Launching meeting (video=$enableVideo, token=${authToken.take(16)}…)")
            val meetingInfo = RtkMeetingInfo(authToken = authToken)
            val uiKitInfo = RealtimeKitUIInfo(activity = activity, rtkMeetingInfo = meetingInfo)
            val rtkUIKit = RealtimeKitUIBuilder.build(uiKitInfo)
            rtkUIKit.startMeeting()
        } catch (e: Throwable) {
            Napier.e(tag = "RealtimeKit", message = "Meeting launch failed: ${e.message}", throwable = e)
            currentOnLeave()
        }
    }

    // Detect when the meeting activity returns control to our host activity and pop CallScreen.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (!launched) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_PAUSE -> hostPaused = true
                Lifecycle.Event.ON_RESUME -> if (hostPaused) {
                    hostPaused = false
                    Napier.i(tag = "RealtimeKit", message = "Host resumed after meeting — popping CallScreen")
                    currentOnLeave()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
