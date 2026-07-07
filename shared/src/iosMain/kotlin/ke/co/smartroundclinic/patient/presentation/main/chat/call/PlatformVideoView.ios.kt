package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIView

@Composable
actual fun LocalVideoPreview(controller: RtkCallController, modifier: Modifier) {
    val videoEnabled by controller.isVideoEnabled
    if (!videoEnabled) return
    val view = controller.session?.localVideoView() ?: return
    UIKitView(
        factory = { view },
        modifier = modifier,
    )
}

@Composable
actual fun RemoteVideoView(controller: RtkCallController, modifier: Modifier) {
    val remote by controller.remoteParticipant
    if (remote == null || remote?.videoEnabled != true) return
    val view = controller.session?.remoteVideoView() ?: return
    UIKitView(
        factory = { view },
        modifier = modifier,
    )
}
