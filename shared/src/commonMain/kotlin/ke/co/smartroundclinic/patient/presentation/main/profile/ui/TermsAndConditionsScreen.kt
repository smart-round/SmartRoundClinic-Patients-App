package ke.co.smartroundclinic.patient.presentation.main.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart

private data class TcSection(val number: Int, val title: String, val body: String)

private val sections = listOf(
    TcSection(1, "Introduction",
        "Welcome to SmartRound Clinic Limited. By using our platform you agree to these Terms and Conditions. " +
        "Please read them carefully before accessing or using our services. These terms govern your use of the app, website, and any related services we provide."
    ),
    TcSection(2, "User Responsibilities",
        "You are responsible for providing accurate information when creating your account and booking appointments. " +
        "You must keep your login credentials confidential and notify us immediately if you suspect unauthorized access. " +
        "You agree not to misuse the platform, submit false medical information, or engage in any activity that disrupts our services."
    ),
    TcSection(3, "Medical Disclaimer",
        "SmartRound Clinic facilitates connections between patients and licensed healthcare professionals. " +
        "We do not provide medical advice directly. All consultations are conducted by independent practitioners. " +
        "Always seek the advice of a qualified physician for any medical condition. Do not disregard professional medical advice because of something you have read or seen on this platform."
    ),
    TcSection(4, "Appointment & Payment",
        "Appointments booked through the platform are subject to availability. Payments are processed securely. " +
        "Cancellations made more than 24 hours in advance are eligible for a full refund. " +
        "Cancellations within 24 hours may attract a fee. SmartRound Clinic reserves the right to modify pricing at any time with reasonable notice."
    ),
    TcSection(5, "Privacy & Cancellations",
        "Your personal and medical data is processed in accordance with our Privacy Policy. We implement industry-standard security measures to protect your information. " +
        "We do not sell your data to third parties. You have the right to request access to, correction of, or deletion of your personal data at any time by contacting our support team."
    ),
)

@Composable
internal fun TermsAndConditionsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0)) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // ── Gradient header ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(brush = Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                    .statusBarsPadding(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Color.White)
                        }
                        Text(
                            text = "Terms & Conditions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center).padding(vertical = 12.dp),
                        )
                    }
                    Text(
                        text = "Please read these terms carefully",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            // ── Content ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .navigationBarsPadding(),
            ) {
                sections.forEach { section ->
                    Text(
                        text = "${section.number}. ${section.title}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = section.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}