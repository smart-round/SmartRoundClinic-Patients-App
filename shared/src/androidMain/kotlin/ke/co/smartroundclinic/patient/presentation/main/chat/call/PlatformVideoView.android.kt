package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cloudflare.realtimekit.platform.VideoView

// VideoView renders/tears down automatically as it attaches/detaches from the window —
// renderVideo()/stopVideoRender() are deprecated leftovers from an older SDK version.
// release() still needs an explicit call to free the native surface.

@Composable
actual fun LocalVideoPreview(controller: RtkCallController, modifier: Modifier) {
    val videoEnabled by controller.isVideoEnabled
    if (!videoEnabled) return
    AndroidView(
        factory = { context -> controller.client?.localUser?.getSelfPreview() ?: VideoView(context) },
        onRelease = { it.release() },
        modifier = modifier,
    )
}

@Composable
actual fun RemoteVideoView(controller: RtkCallController, modifier: Modifier) {
    val remote by controller.remoteParticipant
    if (remote == null || remote?.videoEnabled != true) return
    AndroidView(
        factory = { context ->
            controller.client?.participants?.joined?.firstOrNull()?.getVideoView() ?: VideoView(context)
        },
        onRelease = { it.release() },
        modifier = modifier,
    )
}
