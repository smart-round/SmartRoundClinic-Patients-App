package ke.co.smartroundclinic.patient.presentation.main.chat.call

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.cloudflare.realtimekit.RealtimeKitClient
import com.cloudflare.realtimekit.RealtimeKitMeetingBuilder
import com.cloudflare.realtimekit.RtkMeetingRoomEventListener
import com.cloudflare.realtimekit.RtkParticipant
import com.cloudflare.realtimekit.errors.MeetingError
import com.cloudflare.realtimekit.media.AudioDevice
import com.cloudflare.realtimekit.media.VideoDevice
import com.cloudflare.realtimekit.models.RtkMeetingInfo
import com.cloudflare.realtimekit.network.info.SelfPermissions
import com.cloudflare.realtimekit.participants.RtkParticipants
import com.cloudflare.realtimekit.participants.RtkParticipantsEventListener
import com.cloudflare.realtimekit.participants.RtkRemoteParticipant
import com.cloudflare.realtimekit.self.RtkSelfEventListener
import com.cloudflare.realtimekit.self.RtkSelfParticipant
import com.cloudflare.realtimekit.spotlight.ActiveTab
import com.cloudflare.realtimekit.waitingroom.WaitListStatus
import io.github.aakira.napier.Napier

private const val TAG = "RtkCallController"

actual class RtkCallController(private val activity: Activity) {
    // Exposed internally (same module) so PlatformVideoView.android.kt can pull video views off it.
    internal var client: RealtimeKitClient? = null
        private set

    private val _connectionState = mutableStateOf<CallConnectionState>(CallConnectionState.Connecting)
    actual val connectionState: State<CallConnectionState> = _connectionState

    private val _isAudioEnabled = mutableStateOf(true)
    actual val isAudioEnabled: State<Boolean> = _isAudioEnabled

    private val _isVideoEnabled = mutableStateOf(true)
    actual val isVideoEnabled: State<Boolean> = _isVideoEnabled

    private val _remoteParticipant = mutableStateOf<RemoteParticipantInfo?>(null)
    actual val remoteParticipant: State<RemoteParticipantInfo?> = _remoteParticipant

    private fun onFailed(error: MeetingError) {
        Napier.e(tag = TAG, message = "Call failed: ${error.code} ${error.message}")
        _connectionState.value = CallConnectionState.Failed(error.message)
    }

    private fun toRemoteInfo(p: RtkRemoteParticipant) =
        RemoteParticipantInfo(name = p.name, audioEnabled = p.audioEnabled, videoEnabled = p.videoEnabled)

    private val roomListener = object : RtkMeetingRoomEventListener {
        override fun onMeetingInitStarted() {}
        override fun onMeetingInitCompleted(meeting: RealtimeKitClient) {
            meeting.joinRoom(onSuccess = {}, onFailure = ::onFailed)
        }
        override fun onMeetingInitFailed(error: MeetingError) = onFailed(error)
        override fun onMeetingRoomJoinStarted() {}
        override fun onMeetingRoomJoinCompleted(meeting: RealtimeKitClient) {
            _connectionState.value = CallConnectionState.Connected
            _isAudioEnabled.value = meeting.localUser.audioEnabled
            _isVideoEnabled.value = meeting.localUser.videoEnabled
            meeting.participants.joined.firstOrNull()?.let { _remoteParticipant.value = toRemoteInfo(it) }
            CallForegroundService.start(activity)
        }
        override fun onMeetingRoomJoinFailed(error: MeetingError) = onFailed(error)
        override fun onMeetingRoomLeaveStarted() {}
        override fun onMeetingRoomLeaveCompleted() {
            _connectionState.value = CallConnectionState.Ended
            CallForegroundService.stop(activity)
        }
        override fun onMeetingEnded() {
            _connectionState.value = CallConnectionState.Ended
            CallForegroundService.stop(activity)
        }
        override fun onActiveTabUpdate(meeting: RealtimeKitClient, activeTab: ActiveTab) {}
    }

    private val selfListener = object : RtkSelfEventListener {
        override fun onMeetingRoomJoinedWithoutCameraPermission() {}
        override fun onMeetingRoomJoinedWithoutMicPermission() {}
        override fun onAudioUpdate(isEnabled: Boolean) { _isAudioEnabled.value = isEnabled }
        override fun onVideoUpdate(isEnabled: Boolean) { _isVideoEnabled.value = isEnabled }
        override fun onScreenShareUpdate(isEnabled: Boolean) {}
        override fun onPinned() {}
        override fun onUnpinned() {}
        override fun onAudioDevicesUpdated(devices: List<AudioDevice>) {}
        override fun onAudioDeviceChanged(audioDevice: AudioDevice) {}
        override fun onVideoDeviceChanged(videoDevice: VideoDevice) {}
        override fun onWaitListStatusUpdate(waitListStatus: WaitListStatus) {}
        override fun onUpdate(participant: RtkSelfParticipant) {}
        override fun onRemovedFromMeeting() {
            _connectionState.value = CallConnectionState.Ended
        }
        override fun onScreenShareStartFailed(reason: String) {}
        override fun onPermissionsUpdated(permission: SelfPermissions) {}
    }

    private val participantsListener = object : RtkParticipantsEventListener {
        override fun onParticipantJoin(participant: RtkRemoteParticipant) {
            _remoteParticipant.value = toRemoteInfo(participant)
        }
        override fun onParticipantLeave(participant: RtkRemoteParticipant) {
            if (_remoteParticipant.value?.name == participant.name) _remoteParticipant.value = null
        }
        override fun onAudioUpdate(participant: RtkRemoteParticipant, isEnabled: Boolean) {
            _remoteParticipant.value = toRemoteInfo(participant)
        }
        override fun onVideoUpdate(participant: RtkRemoteParticipant, isEnabled: Boolean) {
            _remoteParticipant.value = toRemoteInfo(participant)
        }
        override fun onScreenShareUpdate(participant: RtkRemoteParticipant, isEnabled: Boolean) {}
        override fun onParticipantPinned(participant: RtkRemoteParticipant) {}
        override fun onParticipantUnpinned(participant: RtkRemoteParticipant) {}
        override fun onActiveParticipantsChanged(active: List<RtkRemoteParticipant>) {}
        override fun onActiveSpeakerChanged(participant: RtkRemoteParticipant?) {}
        override fun onAllParticipantsUpdated(allParticipants: List<RtkParticipant>) {}
        override fun onNewBroadcastMessage(type: String, payload: Map<String, *>) {}
        override fun onUpdate(participants: RtkParticipants) {}
    }

    actual fun start(authToken: String, enableAudio: Boolean, enableVideo: Boolean) {
        // A retry after a failed/ended attempt calls start() again on the same controller —
        // release the stale client first so we don't leak its native resources/listeners.
        client?.let { stale ->
            stale.removeMeetingRoomEventListener(roomListener)
            stale.removeSelfEventListener(selfListener)
            stale.removeParticipantsEventListener(participantsListener)
            stale.release(onSuccess = {}, onFailure = {})
        }
        _connectionState.value = CallConnectionState.Connecting
        val meeting = RealtimeKitMeetingBuilder.build(activity)
        client = meeting
        meeting.addMeetingRoomEventListener(roomListener)
        meeting.addSelfEventListener(selfListener)
        meeting.addParticipantsEventListener(participantsListener)
        val info = RtkMeetingInfo(authToken = authToken, enableAudio = enableAudio, enableVideo = enableVideo)
        meeting.init(info, onSuccess = {}, onFailure = ::onFailed)
    }

    actual fun leaveRoom() {
        client?.leaveRoom(
            onSuccess = { _connectionState.value = CallConnectionState.Ended },
            onFailure = { _connectionState.value = CallConnectionState.Ended },
        )
    }

    actual fun toggleAudio() {
        val user = client?.localUser ?: return
        if (user.audioEnabled) user.disableAudio { } else user.enableAudio { }
    }

    actual fun toggleVideo() {
        val user = client?.localUser ?: return
        if (user.videoEnabled) user.disableVideo { } else user.enableVideo { }
    }

    actual fun switchCamera() {
        client?.localUser?.switchCamera()
    }

    actual fun release() {
        client?.let { meeting ->
            meeting.removeMeetingRoomEventListener(roomListener)
            meeting.removeSelfEventListener(selfListener)
            meeting.removeParticipantsEventListener(participantsListener)
            meeting.release(onSuccess = {}, onFailure = {})
        }
        client = null
        CallForegroundService.stop(activity)
    }
}

@Composable
actual fun rememberRtkCallController(): RtkCallController {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
        ?: error("RtkCallController requires an Activity host context")
    return remember(activity) { RtkCallController(activity) }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
