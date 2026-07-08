package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cloudflare.realtimekit.platform.VideoView

// VideoView renders/tears down automatically as it attaches/detaches from the window —
// renderVideo()/stopVideoRender() are deprecated leftovers from an older SDK version.
// release() still needs an explicit call to free the native surface.
//
// IMPORTANT: these composables must stay mounted for the lifetime of the call once first
// composed — never gate them behind `if (videoEnabled)`/`if (remote != null)` at the call
// site. RealtimeKit hands back the *same* underlying Android View from getSelfPreview()/
// getVideoView() on every call, so removing this AndroidView from composition (e.g. when
// the camera is toggled off) and later re-adding it can race with the view's own detach
// and crash with "The specified child already has a parent". Callers that want to hide
// the feed (camera off, or this participant currently shown as the small tile) should
// draw something on top instead of unmounting this.

@Composable
actual fun LocalVideoPreview(controller: RtkCallController, modifier: Modifier) {
    AndroidView(
        factory = { context -> controller.client?.localUser?.getSelfPreview() ?: VideoView(context) },
        onRelease = { it.release() },
        modifier = modifier,
    )
}

@Composable
actual fun RemoteVideoView(controller: RtkCallController, modifier: Modifier) {
    AndroidView(
        factory = { context ->
            controller.client?.participants?.joined?.firstOrNull()?.getVideoView() ?: VideoView(context)
        },
        onRelease = { it.release() },
        modifier = modifier,
    )
}
