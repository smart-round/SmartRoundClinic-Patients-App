package ke.co.smartroundclinic.patient.presentation.common.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import ke.co.smartroundclinic.patient.presentation.theme.Neutral60
import ke.co.smartroundclinic.patient.presentation.theme.ShapeButton
import ke.co.smartroundclinic.patient.presentation.theme.smartRoundColors

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ShapeButton,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val gradientColors = if (enabled) {
        listOf(
            MaterialTheme.smartRoundColors.buttonGradientStart,
            MaterialTheme.smartRoundColors.buttonGradientEnd,
        )
    } else {
        listOf(Neutral60, Neutral60)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(gradientColors)),
    ) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                content()
            }
        }
    }
}
