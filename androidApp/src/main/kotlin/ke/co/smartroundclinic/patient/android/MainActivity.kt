package ke.co.smartroundclinic.patient.android

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.mmk.kmpnotifier.extensions.onCreateOrOnNewIntent
import com.mmk.kmpnotifier.notification.NotifierManager
import ke.co.smartroundclinic.patient.App
import ke.co.smartroundclinic.patient.presentation.main.chat.call.ActiveCallSignal
import ke.co.smartroundclinic.patient.presentation.main.chat.call.PipModeState
import ke.co.smartroundclinic.patient.presentation.main.chat.call.buildCallPipParams


// Extends FragmentActivity (not ComponentActivity) so the Cloudflare RealtimeKit
// UI Kit can attach its meeting fragment.
class MainActivity : FragmentActivity() {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    private val updateLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { /* user cancelled or update failed — no-op */ }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or denied — FCM handles delivery either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        NotifierManager.onCreateOrOnNewIntent(intent)
        requestNotificationPermission()
        requestFullScreenIntentPermissionIfNeeded()
        setContent {
            App()
        }
        checkForUpdate()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Android 14+ treats full-screen incoming-call notifications as a special permission the
    // user must separately grant in Settings — without it, IncomingCallActivity's ringing UI
    // (see IncomingCallHandler) only shows as a plain heads-up banner. Notification action
    // buttons (Answer/Decline) still work either way; this just gets the fuller call-app UX.
    private fun requestFullScreenIntentPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        val notificationManager = getSystemService(NotificationManager::class.java) ?: return
        if (notificationManager.canUseFullScreenIntent()) return

        // Ask once, ever. Declining leaves canUseFullScreenIntent() false forever, so without
        // remembering that we asked, every cold start threw the user straight into Settings.
        // Calls still work without it — the notification's Answer/Decline buttons are unaffected;
        // it only upgrades the ringing UI to a full-screen call screen.
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_FULL_SCREEN_INTENT_ASKED, false)) return
        prefs.edit().putBoolean(KEY_FULL_SCREEN_INTENT_ASKED, true).apply()

        runCatching {
            startActivity(
                Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        NotifierManager.onCreateOrOnNewIntent(intent)
    }

    // Fires right before the Activity backgrounds from a user action (Home press, app switch,
    // recents) — not on rotation/other config changes. WhatsApp-style: shrink an active video
    // call into a floating PiP window instead of just disappearing behind whatever's next.
    // Audio-only calls have no video worth showing in a PiP window — CallForegroundService
    // already keeps those alive in the background with just a notification.
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (ActiveCallSignal.isConnected.value && ActiveCallSignal.isVideo.value) {
            runCatching { enterPictureInPictureMode(buildCallPipParams(this)) }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        PipModeState.set(isInPictureInPictureMode)
    }

    override fun onResume() {
        super.onResume()
        // Complete a flexible update that finished downloading while the app was in background
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                appUpdateManager.completeUpdate()
            }
        }
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val updateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            when {
                updateAvailable && info.isImmediateUpdateAllowed -> {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                    )
                }
                updateAvailable && info.isFlexibleUpdateAllowed -> {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                    )
                }
            }
        }
    }

    private companion object {
        const val PREFS_NAME = "app_permission_prompts"
        const val KEY_FULL_SCREEN_INTENT_ASKED = "full_screen_intent_asked"
    }
}
