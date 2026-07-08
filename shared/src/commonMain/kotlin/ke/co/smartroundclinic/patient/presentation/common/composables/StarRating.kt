package ke.co.smartroundclinic.patient.presentation.common.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.presentation.theme.Neutral80
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import kotlin.math.roundToInt

@Composable
fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 32.dp,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (index in 1..5) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "$index star${if (index == 1) "" else "s"}",
                tint = if (index <= rating) Primary40 else Neutral80,
                modifier = Modifier
                    .size(starSize)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onRatingChange(index) },
                    ),
            )
        }
    }
}

@Composable
fun StarRatingDisplay(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
) {
    val filled = rating.roundToInt().coerceIn(0, 5)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (index in 1..5) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (index <= filled) Primary40 else Neutral80,
                modifier = Modifier.size(starSize),
            )
        }
    }
}
