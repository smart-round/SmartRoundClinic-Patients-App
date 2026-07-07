package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIView

// IMPORTANT: once the underlying UIView exists, keep this composable mounted for the rest
// of the call — never gate it behind `if (videoEnabled)`/`if (remote != null)` at the call
// site. RealtimeKit hands back the *same* UIView from localVideoView()/remoteVideoView()
// on every call, so removing this from composition (e.g. when the camera is toggled off)
// and later re-adding it can race with the view's own detach and crash. Callers that want
// to hide the feed should draw something on top instead of unmounting this.

@Composable
actual fun LocalVideoPreview(controller: RtkCallController, modifier: Modifier) {
    val view = controller.session?.localVideoView() ?: return
    UIKitView(
        factory = { view },
        modifier = modifier,
    )
}

@Composable
actual fun RemoteVideoView(controller: RtkCallController, modifier: Modifier) {
    val view = controller.session?.remoteVideoView() ?: return
    UIKitView(
        factory = { view },
        modifier = modifier,
    )
}
