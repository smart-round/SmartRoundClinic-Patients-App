package ke.co.smartroundclinic.patient.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.background_pattern
import ke.co.smartroundclinic.patient.generated.resources.splashscreen_logo
import ke.co.smartroundclinic.patient.presentation.theme.smartRoundColors
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.start(
            onOnboarding = onNavigateToOnboarding,
            onSignIn = onNavigateToSignIn,
            onDashboard = onNavigateToDashboard,
        )
    }

    Box(
        contentAlignment = Alignment.Center,
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
        )
        Image(
            painter = painterResource(Res.drawable.splashscreen_logo),
            contentDescription = "Smart Round Clinic",
            contentScale = ContentScale.Crop,
        )
    }
}
