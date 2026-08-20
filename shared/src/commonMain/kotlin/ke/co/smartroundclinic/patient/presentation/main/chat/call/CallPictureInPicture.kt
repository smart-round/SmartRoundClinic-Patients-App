package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * Whether the host window is currently shown as a system Picture-in-Picture surface.
 * Android-only — the iOS actual never flips true; iOS's equivalent widget
 * (`AVPictureInPictureController`) hosts its own separate view controller instead of resizing
 * the app's own window, so [CallScreen] never needs a "compact mode" branch on iOS.
 */
expect object PipModeState {
    val isActive: State<Boolean>
}

/**
 * Returns a function that backgrounds the call screen without ending the call — bound to the
 * explicit in-call minimize button and to the system back gesture ([CallBackHandler]). Android
 * actual enters Picture-in-Picture when [isVideoEnabled] (there's video worth shrinking into a
 * widget) or otherwise just moves the task behind Home (audio-only has no video to show in a
 * PiP window, but the call keeps running via `CallForegroundService`). iOS actual is a no-op —
 * its PiP path starts automatically via `AVPictureInPictureController` on backgrounding, and
 * there's no back-gesture to redirect (see [CallBackHandler]).
 */
@Composable
expect fun rememberMinimizeCallAction(isVideoEnabled: Boolean): () -> Unit

/**
 * Intercepts the system back gesture while [enabled], invoking [onBack] instead of letting it
 * fall through to the default nav-backstack pop. Android actual is `androidx.activity.compose.BackHandler`;
 * iOS has no hardware/system back gesture for this single-Activity Compose app, so the actual is a no-op.
 */
@Composable
expect fun CallBackHandler(enabled: Boolean, onBack: () -> Unit)
