package ke.co.smartroundclinic.patient.presentation.main.chat.call

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import ke.co.smartroundclinic.patient.core.notification.IncomingCallState
import ke.co.smartroundclinic.patient.core.notification.NotificationDeepLink
import ke.co.smartroundclinic.patient.core.notification.NotificationEvent
import ke.co.smartroundclinic.patient.domain.usecase.consultation.DeclineCallUseCase
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.SmartRoundTheme
import ke.co.smartroundclinic.patient.presentation.theme.SnackbarError
import ke.co.smartroundclinic.patient.presentation.theme.SnackbarSuccess
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

// Must match IncomingCallHandler.android.kt / IncomingCallActionReceiver.kt.
private const val CALL_NOTIFICATION_ID = 9001

/**
 * Full-screen ringing UI launched from a full-screen-intent notification (see
 * IncomingCallHandler.android.kt) — shows over the lock screen, plays the device's default
 * ringtone on loop, and vibrates until answered/declined/cancelled. Doctor calling the patient
 * is the only ring direction in this app (the patient app never rings itself), so the caller is
 * always the doctor.
 */
class IncomingCallActivity : ComponentActivity() {

    private val declineCallUseCase: DeclineCallUseCase by inject()
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        val callId = intent.getStringExtra(EXTRA_CALL_ID)
        val callerId = intent.getStringExtra(EXTRA_CALLER_ID)
        if (callId == null || callerId == null) {
            finish()
            return
        }
        val callerName = intent.getStringExtra(EXTRA_CALLER_NAME)
        val isVideo = intent.getBooleanExtra(EXTRA_IS_VIDEO, true)

        // Tapped "Answer" directly on the notification (the fallback path when the OS didn't
        // grant full-screen-intent launch and this Activity only opened via that action button).
        if (intent.getBooleanExtra(EXTRA_AUTO_ACCEPT, false)) {
            acceptCall(callId, callerId, callerName)
            return
        }

        startRinging()

        setContent {
            SmartRoundTheme {
                val current by IncomingCallState.current.collectAsState()
                LaunchedEffect(current) {
                    // Cleared by IncomingCallHandler (answered elsewhere / declined / cancelled / timed out).
                    if (current == null || current?.callId != callId) finish()
                }
                IncomingCallScreen(
                    callerName = callerName ?: "Doctor",
                    isVideo = isVideo,
                    onAccept = {
                        stopRinging()
                        acceptCall(callId, callerId, callerName)
                    },
                    onDecline = {
                        stopRinging()
                        lifecycleScope.launch { declineCallUseCase(callerId, callId) }
                        IncomingCallState.clear(callId)
                        androidx.core.app.NotificationManagerCompat.from(this@IncomingCallActivity).cancel(CALL_NOTIFICATION_ID)
                        finish()
                    },
                )
            }
        }
    }

    private fun acceptCall(callId: String, callerId: String, callerName: String?) {
        NotificationDeepLink.signal(NotificationEvent.ToCall(doctorId = callerId, doctorName = callerName ?: "Doctor", appointmentId = ""))
        packageManager.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
            launchIntent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(launchIntent)
        }
        IncomingCallState.clear(callId)
        androidx.core.app.NotificationManagerCompat.from(this).cancel(CALL_NOTIFICATION_ID)
        finish()
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            )
        }
        val keyguardManager = getSystemService(android.app.KeyguardManager::class.java)
        keyguardManager?.requestDismissKeyguard(this, null)
    }

    private fun startRinging() {
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(this@IncomingCallActivity, uri)
                isLooping = true
                prepare()
                start()
            }
        }
        runCatching {
            val pattern = longArrayOf(0, 1000, 500, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = getSystemService(VibratorManager::class.java)
                vibrator = manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Vibrator::class.java)
            }
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }
    }

    private fun stopRinging() {
        runCatching { mediaPlayer?.stop() }
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
        runCatching { vibrator?.cancel() }
    }

    override fun onDestroy() {
        stopRinging()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_CALL_ID = "callId"
        const val EXTRA_CALLER_ID = "callerId"
        const val EXTRA_CALLER_NAME = "callerName"
        const val EXTRA_DOCTOR_ID = "doctorId"
        const val EXTRA_PATIENT_ID = "patientId"
        const val EXTRA_IS_VIDEO = "isVideo"
        const val EXTRA_AUTO_ACCEPT = "autoAccept"
    }
}

@Composable
private fun IncomingCallScreen(
    callerName: String,
    isVideo: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(64.dp))
        Text(
            text = if (isVideo) "Incoming video call" else "Incoming call",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.White.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = callerName,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CallActionButton(
                icon = Icons.Filled.CallEnd,
                background = SnackbarError,
                contentDescription = "Decline",
                onClick = onDecline,
            )
            CallActionButton(
                icon = if (isVideo) Icons.Filled.Videocam else Icons.Filled.Call,
                background = SnackbarSuccess,
                contentDescription = "Accept",
                onClick = onAccept,
            )
        }
    }
}

@Composable
private fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        colors = IconButtonDefaults.iconButtonColors(containerColor = background),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(32.dp),
        )
    }
}
