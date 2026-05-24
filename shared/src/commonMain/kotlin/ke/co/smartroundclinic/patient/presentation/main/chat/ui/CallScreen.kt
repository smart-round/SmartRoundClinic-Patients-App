package ke.co.smartroundclinic.patient.presentation.main.chat.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.presentation.theme.Error40
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import kotlinx.coroutines.delay

@Composable
internal fun CallScreen(
    doctorName: String,
    isVideo: Boolean,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isVideoOn by remember { mutableStateOf(isVideo) }
    var callSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { while (true) { delay(1000); callSeconds++ } }

    val callTime = "${(callSeconds / 60).toString().padStart(2, '0')}:${(callSeconds % 60).toString().padStart(2, '0')}"

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.6f, animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart), label = "pulseScale")
    val pulseAlpha by infiniteTransition.animateFloat(initialValue = 0.35f, targetValue = 0f, animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart), label = "pulseAlpha")

    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(
                if (isVideoOn) listOf(Color(0xFF0D0D0D), Color(0xFF1A1A1A))
                else listOf(GradientStart, GradientEnd)
            )
        ),
    ) {
        if (isVideoOn) {
            Box(
                modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2B2B2B), Color(0xFF0D0D0D)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(200.dp))
            }
            Box(
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 8.dp, end = 16.dp).size(width = 90.dp, height = 120.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF3A3A3A)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center).padding(bottom = if (isVideoOn) 80.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                Box(modifier = Modifier.size(130.dp).scale(pulseScale).clip(CircleShape).background(Color.White.copy(alpha = pulseAlpha)))
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                }
            }
            Text(text = "Dr. $doctorName", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
            Text(text = callTime, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.75f))
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(bottom = 40.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CallControlButton(icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic, label = if (isMuted) "Unmute" else "Mute", onClick = { isMuted = !isMuted }, active = isMuted)
            CallControlButton(icon = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp, label = "Speaker", onClick = { isSpeakerOn = !isSpeakerOn }, active = isSpeakerOn)
            CallControlButton(icon = Icons.Filled.CallEnd, label = "End", onClick = onEnd, backgroundColor = Error40, size = 64.dp)
            CallControlButton(icon = if (isVideoOn) Icons.Filled.Videocam else Icons.Filled.VideocamOff, label = if (isVideoOn) "Camera" else "Cam Off", onClick = { isVideoOn = !isVideoOn }, active = !isVideoOn)
        }
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f),
    active: Boolean = false,
    size: Dp = 52.dp,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(if (active) Color.White.copy(alpha = 0.35f) else backgroundColor)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(size * 0.45f))
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
    }
}
