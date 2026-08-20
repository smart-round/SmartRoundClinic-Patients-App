package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * Lightweight cross-cutting flag [CallScreen] writes into while a call is active, so platform
 * code that lives outside Compose — Android's `MainActivity.onUserLeaveHint()` (deciding
 * whether to auto-enter Picture-in-Picture) and the PiP window's own mute/end-call
 * `RemoteAction`s (a `BroadcastReceiver`, also outside Compose) — can read/drive the call
 * without needing a full session handle.
 */
object ActiveCallSignal {
    private val _isConnected = mutableStateOf(false)
    val isConnected: State<Boolean> = _isConnected

    private val _isVideo = mutableStateOf(false)
    val isVideo: State<Boolean> = _isVideo

    private val _isAudioEnabled = mutableStateOf(true)
    val isAudioEnabled: State<Boolean> = _isAudioEnabled

    var onToggleAudio: (() -> Unit)? = null
    var onEndCall: (() -> Unit)? = null

    fun update(isConnected: Boolean, isVideo: Boolean, isAudioEnabled: Boolean) {
        _isConnected.value = isConnected
        _isVideo.value = isVideo
        _isAudioEnabled.value = isAudioEnabled
    }

    fun clear() {
        _isConnected.value = false
        _isVideo.value = false
        onToggleAudio = null
        onEndCall = null
    }
}
