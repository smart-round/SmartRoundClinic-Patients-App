package ke.co.smartroundclinic.patient.presentation.main.chat.call

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders the local camera preview backed by [controller]'s active RealtimeKit session.
 *
 * [isFrontmost] must be true whenever this view is the small floating tile overlapping the
 * other participant's full-screen video — see the note on `bringToFront()` in the platform
 * actuals for why this can't be left to Compose's `Modifier.zIndex`.
 */
@Composable
expect fun LocalVideoPreview(controller: RtkCallController, isFrontmost: Boolean, modifier: Modifier)

/** Renders the connected remote participant's video stream. See [LocalVideoPreview] for [isFrontmost]. */
@Composable
expect fun RemoteVideoView(controller: RtkCallController, isFrontmost: Boolean, modifier: Modifier)
