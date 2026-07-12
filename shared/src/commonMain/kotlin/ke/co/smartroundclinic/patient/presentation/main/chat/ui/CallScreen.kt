package ke.co.smartroundclinic.patient.presentation.main.chat.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.CallJoinInfo
import ke.co.smartroundclinic.patient.presentation.main.chat.call.CallConnectionState
import ke.co.smartroundclinic.patient.presentation.main.chat.call.LocalVideoPreview
import ke.co.smartroundclinic.patient.presentation.main.chat.call.RemoteVideoView
import ke.co.smartroundclinic.patient.presentation.main.chat.call.rememberRtkCallController
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import kotlinx.coroutines.delay

@Composable
internal fun CallScreen(
    doctorName: String,
    doctorPicture: String? = null,
    selfPicture: String? = null,
    isVideo: Boolean,
    joinState: Resource<CallJoinInfo>?,
    onJoin: () -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { if (joinState == null) onJoin() }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        when (joinState) {
            is Resource.Success -> {
                val info = joinState.data
                if (info == null) {
                    CallStatus("No join info received") { onEnd() }
                } else {
                    ActiveCall(
                        doctorName = doctorName,
                        doctorPicture = doctorPicture,
                        selfPicture = selfPicture,
                        authToken = info.authToken,
                        isVideo = isVideo,
                        onEnd = onEnd,
                    )
                }
            }
            is Resource.Error -> CallStatus("Failed to join: ${joinState.message ?: "unknown error"}") { onEnd() }
            is Resource.Loading, null -> CallConnecting(doctorName)
        }
    }
}

@Composable
private fun ActiveCall(
    doctorName: String,
    doctorPicture: String?,
    selfPicture: String?,
    authToken: String,
    isVideo: Boolean,
    onEnd: () -> Unit,
) {
    val controller = rememberRtkCallController()

    LaunchedEffect(authToken) {
        controller.start(authToken = authToken, enableAudio = true, enableVideo = isVideo)
    }
    DisposableEffect(controller) {
        onDispose { controller.release() }
    }

    val connectionState by controller.connectionState
    val isAudioEnabled by controller.isAudioEnabled
    val isVideoEnabled by controller.isVideoEnabled
    val remote by controller.remoteParticipant

    LaunchedEffect(connectionState) {
        if (connectionState is CallConnectionState.Ended) onEnd()
    }

    // Persistent call-duration timer — starts ticking once connected, keeps running
    // regardless of which participant is currently shown as primary.
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(connectionState) {
        if (connectionState is CallConnectionState.Connected) {
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    // WhatsApp-style flip — tapping the small floating tile swaps who's shown full-screen.
    var selfIsPrimary by remember { mutableStateOf(false) }

    when (val state = connectionState) {
        is CallConnectionState.Connecting -> CallConnecting(doctorName)
        is CallConnectionState.Failed -> CallStatus("Call failed: ${state.message}") { onEnd() }
        is CallConnectionState.Ended -> Unit // onEnd() fired above; avoid flashing content while popping
        is CallConnectionState.Connected -> {
            val remoteName = remote?.name ?: "Dr. $doctorName"
            val remoteHasVideo = remote?.videoEnabled == true
            val remoteAudioEnabled = remote?.audioEnabled ?: false
            val hasRemote = remote != null

            // Once the remote participant has joined at least once, keep their tile mounted
            // for the rest of the call — RealtimeKit hands back the same underlying native
            // video view on every call, so removing/re-adding this ParticipantView (and
            // therefore its video content) if `remote` ever transiently drops to null (e.g. a
            // brief reconnect reported as leave-then-rejoin) crashes when Compose tries to
            // re-parent that view mid-detach. `remoteHasVideo`/`remoteAudioEnabled` above
            // already fall back to sane defaults when `remote` is momentarily null, so the
            // tile just shows a muted avatar state through the blip instead of disappearing.
            var everHadRemote by remember { mutableStateOf(false) }
            if (hasRemote) everHadRemote = true

            // If the doctor hasn't joined yet, self is always shown full-screen — there's
            // nothing to flip to. Once they join, `selfIsPrimary` takes over.
            val effectiveSelfPrimary = selfIsPrimary || !everHadRemote

            val fullScreenModifier = Modifier.fillMaxSize()

            Box(modifier = Modifier.fillMaxSize()) {
                // No .clip() here — RealtimeKit's video surface is backed by WebRTC's
                // SurfaceViewRenderer (a hardware-overlay SurfaceView on Android), which
                // renders through a separate compositing layer that ignores Compose/View clip
                // paths entirely. Clipping this container rounds everything else correctly but
                // makes the video itself render blank/white. Square corners on the small tile
                // is the trade-off until/unless RealtimeKit exposes a TextureView-backed
                // renderer option.
                val smallTileModifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .size(width = 110.dp, height = 150.dp)

                // Both participants' video views are mounted exactly once and stay mounted
                // for the rest of the call — RealtimeKit hands back the same underlying
                // native view from getSelfPreview()/getVideoView() each time, so
                // conditionally adding/removing them from the tree (e.g. to swap primary vs
                // secondary, or hide/show on camera toggle) crashes when Compose tries to
                // re-parent a view mid-detach. Flipping and muting are handled entirely via
                // size/z-order/overlay instead.
                ParticipantView(
                    picture = selfPicture,
                    label = "You",
                    audioEnabled = isAudioEnabled,
                    showVideo = isVideoEnabled,
                    videoContent = { LocalVideoPreview(controller, isFrontmost = !effectiveSelfPrimary, modifier = it) },
                    avatarSize = if (effectiveSelfPrimary) 112.dp else 56.dp,
                    modifier = (if (effectiveSelfPrimary) fullScreenModifier else smallTileModifier)
                        .zIndex(if (effectiveSelfPrimary) 0f else 1f)
                        .then(if (!effectiveSelfPrimary) Modifier.clickable { selfIsPrimary = true } else Modifier),
                )

                if (everHadRemote) {
                    ParticipantView(
                        picture = doctorPicture,
                        label = remoteName,
                        audioEnabled = remoteAudioEnabled,
                        showVideo = remoteHasVideo,
                        videoContent = { RemoteVideoView(controller, isFrontmost = effectiveSelfPrimary, modifier = it) },
                        avatarSize = if (effectiveSelfPrimary) 56.dp else 112.dp,
                        modifier = (if (effectiveSelfPrimary) smallTileModifier else fullScreenModifier)
                            .zIndex(if (effectiveSelfPrimary) 1f else 0f)
                            .then(if (effectiveSelfPrimary) Modifier.clickable { selfIsPrimary = false } else Modifier),
                    )
                }

                Column(modifier = Modifier.statusBarsPadding().padding(16.dp).align(Alignment.TopStart).zIndex(2f)) {
                    Text(
                        text = if (effectiveSelfPrimary) "You" else remoteName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatCallDuration(elapsedSeconds),
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                CallControls(
                    isAudioEnabled = isAudioEnabled,
                    isVideoEnabled = isVideoEnabled,
                    onToggleAudio = controller::toggleAudio,
                    onToggleVideo = controller::toggleVideo,
                    onSwitchCamera = controller::switchCamera,
                    onEndCall = {
                        controller.leaveRoom()
                        onEnd()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

/** Renders a participant's video feed, or a branded avatar card with a speaking pulse when their camera is off. */
@Composable
private fun ParticipantView(
    picture: String?,
    label: String,
    audioEnabled: Boolean,
    showVideo: Boolean,
    videoContent: @Composable (Modifier) -> Unit,
    avatarSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))) {
        // Always mounted — see the note on LocalVideoPreview/RemoteVideoView for why this
        // must never be conditionally added/removed. On iOS, a *visible* (non-zero-size)
        // interop view punches a permanent transparent hole through the whole Compose canvas
        // at its bounds, so Compose content drawn "over" it (the avatar below) would never
        // actually render there — shrinking it to near-zero size when the camera is off keeps
        // it mounted without reserving any visible/cutout space.
        videoContent(if (showVideo) Modifier.fillMaxSize() else Modifier.size(1.dp))

        if (!showVideo) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        SoundWavePulse(active = audioEnabled, baseSize = avatarSize)
                        DoctorAvatar(picture = picture, size = avatarSize.value.toInt())
                    }
                    Text(
                        text = label,
                        color = Color.White,
                        style = if (avatarSize >= 96.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Two staggered expanding-and-fading rings behind an avatar — a "speaking" indicator while unmuted. */
@Composable
private fun SoundWavePulse(active: Boolean, baseSize: Dp) {
    if (!active) return
    val transition = rememberInfiniteTransition(label = "soundwave")

    val scale1 by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "scale1",
    )
    val alpha1 by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "alpha1",
    )
    val scale2 by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            initialStartOffset = StartOffset(700, StartOffsetType.Delay),
        ),
        label = "scale2",
    )
    val alpha2 by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            initialStartOffset = StartOffset(700, StartOffsetType.Delay),
        ),
        label = "alpha2",
    )

    Box(modifier = Modifier.size(baseSize * 1.6f), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(baseSize * scale1).clip(CircleShape).background(Color.White.copy(alpha = alpha1)))
        Box(modifier = Modifier.size(baseSize * scale2).clip(CircleShape).background(Color.White.copy(alpha = alpha2)))
    }
}

private fun formatCallDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val mm = minutes.toString().padStart(2, '0')
    val ss = seconds.toString().padStart(2, '0')
    return if (hours > 0) "$hours:$mm:$ss" else "$mm:$ss"
}

@Composable
private fun CallControls(
    isAudioEnabled: Boolean,
    isVideoEnabled: Boolean,
    onToggleAudio: () -> Unit,
    onToggleVideo: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        CallControlButton(
            icon = if (isAudioEnabled) Icons.Filled.Mic else Icons.Filled.MicOff,
            active = isAudioEnabled,
            contentDescription = "Toggle microphone",
            onClick = onToggleAudio,
        )
        Row(modifier = Modifier.width(20.dp)) {}
        CallControlButton(
            icon = if (isVideoEnabled) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
            active = isVideoEnabled,
            contentDescription = "Toggle camera",
            onClick = onToggleVideo,
        )
        Row(modifier = Modifier.width(20.dp)) {}
        CallControlButton(
            icon = Icons.Filled.Cameraswitch,
            active = true,
            contentDescription = "Switch camera",
            onClick = onSwitchCamera,
        )
        Row(modifier = Modifier.width(20.dp)) {}
        CallControlButton(
            icon = Icons.Filled.CallEnd,
            active = true,
            containerColor = MaterialTheme.colorScheme.error,
            contentDescription = "End call",
            onClick = onEndCall,
        )
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    active: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = if (active) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.9f),
) {
    val tint = if (containerColor == MaterialTheme.colorScheme.error || active) Color.White else Color.Black
    IconButton(
        onClick = onClick,
        modifier = modifier.size(56.dp).clip(CircleShape).background(containerColor),
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint)
    }
}

@Composable
private fun CallConnecting(doctorName: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GradientStart, GradientEnd))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(color = Color.White)
            Text(
                text = "Connecting to Dr. $doctorName…",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun CallStatus(message: String, onClose: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.08f)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = message, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onClose) { Text("Close") }
        }
    }
}
