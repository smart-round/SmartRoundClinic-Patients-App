package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * Thin wrapper around RealtimeKit's Core SDK — owns a single call's lifecycle and
 * exposes Compose-observable state so [ke.co.smartroundclinic.patient.presentation.main.chat.ui.CallScreen]
 * can render its own video grid/controls instead of delegating to RealtimeKit's prebuilt UI Kit.
 *
 * Obtain an instance via [rememberRtkCallController] — the Android actual needs the
 * host Activity, which is only available inside composition.
 */
expect class RtkCallController {
    val connectionState: State<CallConnectionState>
    val isAudioEnabled: State<Boolean>
    val isVideoEnabled: State<Boolean>
    val remoteParticipant: State<RemoteParticipantInfo?>

    /** Initializes the SDK with [authToken] and joins the room once init succeeds. */
    fun start(authToken: String, enableAudio: Boolean, enableVideo: Boolean)
    fun leaveRoom()
    fun toggleAudio()
    fun toggleVideo()
    fun switchCamera()

    /** Releases native SDK resources — call when the call screen leaves composition. */
    fun release()
}

@Composable
expect fun rememberRtkCallController(): RtkCallController
