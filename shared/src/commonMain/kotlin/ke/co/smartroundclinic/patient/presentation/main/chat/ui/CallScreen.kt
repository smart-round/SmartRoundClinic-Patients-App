package ke.co.smartroundclinic.patient.presentation.main.chat.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.CallJoinInfo
import ke.co.smartroundclinic.patient.presentation.main.chat.call.CallConnectionState
import ke.co.smartroundclinic.patient.presentation.main.chat.call.LocalVideoPreview
import ke.co.smartroundclinic.patient.presentation.main.chat.call.RemoteVideoView
import ke.co.smartroundclinic.patient.presentation.main.chat.call.rememberRtkCallController
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Neutral20

@Composable
internal fun CallScreen(
    doctorName: String,
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

    when (val state = connectionState) {
        is CallConnectionState.Connecting -> CallConnecting(doctorName)
        is CallConnectionState.Failed -> CallStatus("Call failed: ${state.message}") { onEnd() }
        is CallConnectionState.Ended -> Unit // onEnd() fired above; avoid flashing content while popping
        is CallConnectionState.Connected -> {
            Box(modifier = Modifier.fillMaxSize()) {
                // Remote participant fills the screen; falls back to an avatar placeholder
                // until they join or if their camera is off.
                if (remote?.videoEnabled == true) {
                    RemoteVideoView(controller, modifier = Modifier.fillMaxSize())
                } else {
                    ParticipantPlaceholder(name = remote?.name ?: "Dr. $doctorName")
                }

                // Local self-preview — small floating tile, top-right.
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .size(width = 110.dp, height = 150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Neutral20),
                ) {
                    if (isVideoEnabled) {
                        LocalVideoPreview(controller, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }

                Text(
                    text = remote?.name ?: "Dr. $doctorName",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.statusBarsPadding().padding(16.dp).align(Alignment.TopStart),
                )

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
private fun ParticipantPlaceholder(name: String) {
    Box(modifier = Modifier.fillMaxSize().background(Neutral20), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(96.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Text(text = name, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
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
