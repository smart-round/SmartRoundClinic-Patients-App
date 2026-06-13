package ke.co.smartroundclinic.patient.presentation.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.background_pattern
import ke.co.smartroundclinic.patient.generated.resources.password_reset_success
import ke.co.smartroundclinic.patient.presentation.theme.smartRoundColors
import org.jetbrains.compose.resources.painterResource

@Composable
fun PasswordResetSuccessScreen(
    onNavigateToLoginScreen: () -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }

    val offsetY = remember { Animatable(-200f) }
    val imageScale = remember { Animatable(0.5f) }
    val rotation = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatingOffset",
    )

    LaunchedEffect(Unit) {
        isVisible = true
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow),
        )
        rotation.animateTo(targetValue = 10f, animationSpec = spring(Spring.DampingRatioHighBouncy))
        rotation.animateTo(targetValue = 0f, animationSpec = spring(Spring.DampingRatioMediumBouncy))
    }

    LaunchedEffect(Unit) {
        imageScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessVeryLow,
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.smartRoundColors.gradientStart,
                        MaterialTheme.smartRoundColors.gradientEnd,
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(Res.drawable.background_pattern),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1000)),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.safeDrawing.asPaddingValues()),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(32.dp)
                        .background(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.onPrimary),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.password_reset_success),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.graphicsLayer {
                                translationY = offsetY.value + floatingOffset
                                scaleX = imageScale.value
                                scaleY = imageScale.value
                                rotationZ = rotation.value
                            },
                        )
                    }

                    Text(
                        text = "Password Changed!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                    )
                    Text(
                        text = "You have successfully created a new password. You can now sign in with your new credentials",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(bottom = 16.dp),
                    )

                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.smartRoundColors.statusPublished,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = MaterialTheme.shapes.medium,
                        onClick = onNavigateToLoginScreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                    ) {
                        Text(text = "Sign In", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
