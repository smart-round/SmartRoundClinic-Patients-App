package ke.co.smartroundclinic.patient.presentation.main.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific Cloudflare RealtimeKit meeting UI.
 *
 *  - **Android** — embeds `RtkMeetingActivity`-style fragment/UI from the
 *    `io.dyte:uikit` (Cloudflare RealtimeKit) SDK.
 *  - **iOS** — hosts the `RealtimeKitUI` Swift Package's view controller via
 *    [androidx.compose.ui.interop.UIKitViewController].
 *
 * @param authToken participant join token returned by the backend
 *                  `POST /consultation/{id}/call/join` endpoint.
 * @param enableVideo whether to start with camera enabled.
 * @param onLeave invoked once when the participant leaves or the meeting ends.
 */
@Composable
expect fun RealtimeMeetingView(
    authToken: String,
    enableVideo: Boolean,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
)
