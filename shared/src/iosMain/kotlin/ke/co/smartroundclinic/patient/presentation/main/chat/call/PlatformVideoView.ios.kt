package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIView

// IMPORTANT: once the underlying UIView exists, keep this composable mounted for the rest
// of the call — never gate it behind `if (videoEnabled)`/`if (remote != null)` at the call
// site. RealtimeKit hands back the *same* UIView from localVideoView()/remoteVideoView()
// on every call, so removing this from composition (e.g. when the camera is toggled off)
// and later re-adding it can race with the view's own detach and crash. Callers that want
// to hide the feed should draw something on top instead of unmounting this.
//
// isInteractive = false: the video feed itself has no native gestures of its own, and by
// default Compose Multiplatform's UIKitView uses "Cooperative" touch handling, which hands
// a stationary tap over to the native view entirely once the video fills the tappable area
// (e.g. the WhatsApp-style flip tile) — silently swallowing Modifier.clickable on the Box
// wrapping it. Marking it non-interactive routes all touches through Compose instead.
private val videoInteropProperties =
    UIKitInteropProperties(isInteractive = false, isNativeAccessibilityEnabled = false)

// Both participants' video surfaces share the same interop container Compose uses for all
// UIKitViews, added in first-composed order — self is always composed before remote in
// CallScreen, so self's native view permanently sits behind remote's in the real UIKit view
// hierarchy, regardless of Compose's Modifier.zIndex (which only reorders Compose's own
// canvas-drawn content, not interop-hosted UIViews). Without this, whichever participant is
// demoted to the small floating tile renders behind the other's full-screen video. Bring the
// tile's own view to the front of its superview whenever it becomes the small tile so the real
// view order always matches which one visually needs to be on top.
private fun UIView.bringToFrontOfSuperview() {
    superview?.bringSubviewToFront(this)
}

private fun UIView.sendToBackOfSuperview() {
    superview?.sendSubviewToBack(this)
}

// RealtimeKit tears down and rebuilds the WebRTC transport's consumers/producers on network-drop
// recovery (MediaRoomController.reconnectTransport — closes the transport, creates a new one, and
// for the receive side rebuilds all consumers from scratch), which can hand back a genuinely new
// native view from localVideoView()/remoteVideoView() afterwards. But UIKitView's factory only
// runs once per node's lifetime (see the notes on each composable below), so without forcing a
// fresh node post-recovery, the old — now-dead — view stays mounted forever: a frozen last frame
// that never resumes, which is exactly the "video hangs after a network drop" symptom this fixes.
// Bumping this key only on the true->false edge (recovery, not entry) means the node rebuilds
// once reconnection is actually done, not while it's still in progress.
@Composable
private fun rememberMediaGeneration(controller: RtkCallController): Int {
    val isReconnecting by controller.isReconnecting
    var wasReconnecting by remember { mutableStateOf(false) }
    var generation by remember { mutableIntStateOf(0) }
    if (wasReconnecting && !isReconnecting) generation++
    wasReconnecting = isReconnecting
    return generation
}

@Composable
actual fun LocalVideoPreview(controller: RtkCallController, isFrontmost: Boolean, modifier: Modifier) {
    val isVideoEnabled by controller.isVideoEnabled
    val view = controller.session?.localVideoView() ?: return
    // See rememberMediaGeneration's doc comment above — forces a fresh UIKitView node (and thus
    // a fresh factory call) once reconnection resolves, in case the producer transport was rebuilt.
    key(rememberMediaGeneration(controller)) {
        UIKitView(
            factory = { view },
            modifier = modifier,
            // Only bring this view forward while it's actually showing real video — otherwise
            // send it to the back so the avatar-card overlay ParticipantView draws in its place
            // (normal Compose-drawn content, not an interop view) reliably covers it instead of a
            // frozen last frame staying visible on top of the "camera off" placeholder.
            update = { if (isFrontmost && isVideoEnabled) it.bringToFrontOfSuperview() else it.sendToBackOfSuperview() },
            properties = videoInteropProperties,
        )
    }
}

@Composable
actual fun RemoteVideoView(controller: RtkCallController, isFrontmost: Boolean, modifier: Modifier) {
    // Mirrors the Android actual's fix: wait for the SDK to confirm the remote participant's
    // video is actually enabled (driven by RtkCallController's onVideoUpdate) before mounting
    // the UIKitView at all, then latch permanently — don't rely solely on remoteVideoView()'s
    // own null-vs-non-null behavior at join time, since RealtimeKit can hand back a view before
    // its track is actually bound, and UIKitView's factory only runs once per node's lifetime.
    val remote by controller.remoteParticipant
    var everHadVideo by remember { mutableStateOf(false) }
    if (remote?.videoEnabled == true) everHadVideo = true
    if (!everHadVideo) return

    val view = controller.session?.remoteVideoView() ?: return
    key(rememberMediaGeneration(controller)) {
        UIKitView(
            factory = { view },
            modifier = modifier,
            // Same reasoning as LocalVideoPreview above — also covers the participant leaving
            // entirely (remote becomes null, videoEnabled reads false): without this, their last
            // frame stays visible on top of the "gone" placeholder instead of being covered by it.
            update = { if (isFrontmost && remote?.videoEnabled == true) it.bringToFrontOfSuperview() else it.sendToBackOfSuperview() },
            properties = videoInteropProperties,
        )
    }
}
