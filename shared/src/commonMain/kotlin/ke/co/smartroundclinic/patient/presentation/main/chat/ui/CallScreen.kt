package ke.co.smartroundclinic.patient.presentation.main.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.CallJoinInfo
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart

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

    Box(modifier = modifier.fillMaxSize()) {
        when (joinState) {
            is Resource.Success -> {
                val info = joinState.data
                if (info == null) {
                    CallStatus("No join info received") { onEnd() }
                } else {
                    RealtimeMeetingView(
                        authToken = info.authToken,
                        enableVideo = isVideo,
                        onLeave = onEnd,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            is Resource.Error -> CallStatus("Failed to join: ${joinState.message ?: "unknown error"}") { onEnd() }
            is Resource.Loading, null -> CallConnecting(doctorName)
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
