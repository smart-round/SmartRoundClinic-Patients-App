package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

// iOS's video-call PiP widget (AVPictureInPictureController, wired up natively in
// iosApp/iosApp/CallPictureInPictureManager.swift) hosts its own separate view controller
// rather than resizing this app's own window, so CallScreen never needs a "compact mode"
// branch here the way Android's system PiP requires.
actual object PipModeState {
    actual val isActive: State<Boolean> = mutableStateOf(false)
}

@Composable
actual fun rememberMinimizeCallAction(isVideoEnabled: Boolean): () -> Unit = remember { {} }

@Composable
actual fun CallBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No system/hardware back gesture to intercept — this single-ViewController Compose
    // Multiplatform app has no UINavigationController-driven swipe-back.
}
