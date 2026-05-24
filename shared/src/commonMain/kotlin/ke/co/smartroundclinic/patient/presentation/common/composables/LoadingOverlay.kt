package ke.co.smartroundclinic.patient.presentation.common.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Semi-transparent overlay with a centered spinner. Place inside a Box that
 * already holds the screen content so the spinner floats on top and blocks
 * touches while an API call is in progress.
 *
 * Usage:
 * ```
 * Box(modifier = Modifier.fillMaxSize()) {
 *     ScreenContent(...)
 *     if (isLoading) LoadingOverlay()
 * }
 * ```
 */
@Composable
fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.38f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(44.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Full-screen centered spinner for screens where content has not loaded yet
 * (first fetch, no cached data to show behind it).
 *
 * Usage:
 * ```
 * if (isLoading && items.isEmpty()) {
 *     FullScreenLoading()
 * } else {
 *     ContentList(items)
 * }
 * ```
 */
@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(44.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
