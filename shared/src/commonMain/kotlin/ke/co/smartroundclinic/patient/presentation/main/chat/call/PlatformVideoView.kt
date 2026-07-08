package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Renders the local camera preview backed by [controller]'s active RealtimeKit session. */
@Composable
expect fun LocalVideoPreview(controller: RtkCallController, modifier: Modifier)

/** Renders the connected remote participant's video stream. */
@Composable
expect fun RemoteVideoView(controller: RtkCallController, modifier: Modifier)
