package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

actual class RtkCallController {
    // Exposed internally (same module) so PlatformVideoView.ios.kt can pull video views off it.
    internal var session: IosCallSession? = null
        private set

    private val _connectionState = mutableStateOf<CallConnectionState>(CallConnectionState.Connecting)
    actual val connectionState: State<CallConnectionState> = _connectionState

    private val _isAudioEnabled = mutableStateOf(true)
    actual val isAudioEnabled: State<Boolean> = _isAudioEnabled

    private val _isVideoEnabled = mutableStateOf(true)
    actual val isVideoEnabled: State<Boolean> = _isVideoEnabled

    private val _remoteParticipant = mutableStateOf<RemoteParticipantInfo?>(null)
    actual val remoteParticipant: State<RemoteParticipantInfo?> = _remoteParticipant

    private val _isReconnecting = mutableStateOf(false)
    actual val isReconnecting: State<Boolean> = _isReconnecting

    private val listener = object : IosCallSessionListener {
        override fun onConnected() { _connectionState.value = CallConnectionState.Connected }
        override fun onEnded() { _connectionState.value = CallConnectionState.Ended }
        override fun onFailed(message: String) {
            _isReconnecting.value = false
            _connectionState.value = CallConnectionState.Failed(message)
        }
        override fun onAudioUpdate(enabled: Boolean) { _isAudioEnabled.value = enabled }
        override fun onVideoUpdate(enabled: Boolean) { _isVideoEnabled.value = enabled }
        override fun onRemoteParticipantUpdate(name: String?, audioEnabled: Boolean, videoEnabled: Boolean) {
            _remoteParticipant.value = name?.let { RemoteParticipantInfo(it, audioEnabled, videoEnabled) }
        }
        override fun onReconnecting(isReconnecting: Boolean) {
            // Ignore a reconnecting=true blip before the call has ever connected — the initial
            // join handshake already has its own Connecting UI; only a mid-call drop matters here.
            if (!isReconnecting || _connectionState.value is CallConnectionState.Connected) {
                _isReconnecting.value = isReconnecting
            }
        }
    }

    actual fun start(authToken: String, enableAudio: Boolean, enableVideo: Boolean) {
        // A retry after a failed/ended attempt calls start() again on the same controller —
        // dispose the stale session first so we don't leak its native resources.
        session?.dispose()
        val factory = RtkCallBridge.factory
        if (factory == null) {
            _connectionState.value = CallConnectionState.Failed("RealtimeKit bridge not wired — see iOSApp.swift")
            return
        }
        _connectionState.value = CallConnectionState.Connecting
        _isReconnecting.value = false
        session = factory(authToken, enableAudio, enableVideo, listener)
    }

    actual fun leaveRoom() {
        session?.leaveRoom()
    }

    actual fun toggleAudio() {
        session?.toggleAudio()
    }

    actual fun toggleVideo() {
        session?.toggleVideo()
    }

    actual fun switchCamera() {
        session?.switchCamera()
    }

    actual fun release() {
        session?.dispose()
        session = null
    }
}

@Composable
actual fun rememberRtkCallController(): RtkCallController = remember { RtkCallController() }
