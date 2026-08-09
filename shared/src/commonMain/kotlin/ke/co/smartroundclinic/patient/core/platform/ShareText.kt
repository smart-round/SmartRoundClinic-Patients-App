package ke.co.smartroundclinic.patient.core.platform

import androidx.compose.runtime.Composable

/**
 * Hands a plain-text blob to the platform share sheet — Android's chooser, iOS's
 * `UIActivityViewController`. Composable rather than a plain `expect fun` because both platforms
 * need something only the UI tree can hand out (an Activity `Context`, a root view controller).
 */
@Composable
expect fun rememberShareText(): (String) -> Unit
