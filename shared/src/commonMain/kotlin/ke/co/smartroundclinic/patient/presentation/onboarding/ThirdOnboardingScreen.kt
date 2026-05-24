package ke.co.smartroundclinic.patient.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.old_male_doctor
import ke.co.smartroundclinic.patient.generated.resources.onboarding_screen_bg
import ke.co.smartroundclinic.patient.presentation.common.composables.PrimaryButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun ThirdOnboardingScreen(
    onNavigateToNext: () -> Unit,
    onSkipToNext: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(Res.drawable.onboarding_screen_bg),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "Everything stays organized.",
                    style = MaterialTheme.typography.headlineLarge,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Access past consultations, prescriptions, and records in one secure place, supporting informed, continuous care.",
                    maxLines = 3,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Image(
                painter = painterResource(Res.drawable.old_male_doctor),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
            Column {
                PrimaryButton(
                    onClick = onNavigateToNext,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}
