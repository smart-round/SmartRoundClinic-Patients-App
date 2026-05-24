package ke.co.smartroundclinic.patient.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.female_young_doctor
import ke.co.smartroundclinic.patient.generated.resources.onboarding_screen_bg
import ke.co.smartroundclinic.patient.presentation.common.composables.PrimaryButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun SecondOnboardingScreen(
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
                    text = "Confidential care, delivered digitally.",
                    style = MaterialTheme.typography.headlineLarge,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Engage with your doctor through secure video, voice or chat. ",
                    maxLines = 3,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Image(
                painter = painterResource(Res.drawable.female_young_doctor),
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
                        text = "Next",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                TextButton(
                    onClick = onSkipToNext,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Skip", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
