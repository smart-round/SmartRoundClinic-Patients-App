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

private data class PolicySection(val title: String, val body: String)

private val policySections = listOf(
    PolicySection("1. Introduction",
        """SmartRound Clinic Limited ("SmartRound", "we", "our") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, share, and safeguard your personal information when you use our telemedicine platform, mobile application, and related services. By using our services you consent to the practices described in this policy. Last updated: June 2026."""
    ),
    PolicySection("2. Information We Collect",
        "We collect information you provide directly: full name, email address, phone number, date of birth, gender, and profile photo. During consultations we collect symptom descriptions, medical history, and chat/call records. We also automatically collect device information (device model, OS version, IP address), usage data, and approximate location for service routing and fraud prevention."
    ),
    PolicySection("3. How We Use Your Information",
        "We use your personal data to: (a) create and manage your account; (b) match you with appropriate healthcare providers; (c) process and confirm appointment bookings and payments; (d) facilitate video or audio consultations and share consultation notes; (e) send appointment reminders, receipts, and service updates; (f) improve our platform, conduct research, and detect fraud; (g) comply with applicable Kenyan law and regulatory requirements."
    ),
    PolicySection("4. Medical Data & Confidentiality",
        "Your health information is classified as sensitive personal data. We apply strict technical and organisational controls to protect it. Consultation recordings and transcripts are encrypted at rest and in transit. Only you and your attending doctor can access your medical records through the platform. We do not use your health data for advertising purposes."
    ),
    PolicySection("5. Sharing Your Information",
        "We do not sell your personal data. We share your information only in the following circumstances: with the licensed doctor you book a consultation with; with our verified payment processor (IntaSend) to complete transactions; with technology service providers (cloud hosting, analytics) under strict data-processing agreements; and when required by law, court order, or regulatory authority in Kenya."
    ),
    PolicySection("6. Data Retention",
        "We retain your account and medical data for as long as your account is active or as required by Kenyan health regulations. If you delete your account, we will remove your personal data within 30 days, except where retention is required by law (e.g., financial records retained for 7 years under the Income Tax Act)."
    ),
    PolicySection("7. Your Rights",
        "Under the Kenya Data Protection Act 2019 you have the right to: access the personal data we hold about you; correct inaccurate data; request deletion of your data; object to or restrict processing; and lodge a complaint with the Office of the Data Protection Commissioner (ODPC). To exercise any of these rights, contact us at privacy@smartroundclinic.co.ke."
    ),
    PolicySection("8. Cookies & Tracking",
        "Our app does not use advertising cookies or third-party tracking pixels. We use analytics solely to improve app performance and user experience. You can opt out of analytics in app settings at any time."
    ),
    PolicySection("9. Children's Privacy",
        "SmartRound Clinic is not intended for users under 18 without parental consent. We do not knowingly collect personal data from minors. If you believe a minor has registered without consent, please contact us immediately and we will delete the account."
    ),
    PolicySection("10. Changes to This Policy",
        "We may update this Privacy Policy from time to time. We will notify you of material changes via in-app notification or email at least 14 days before they take effect. Continued use of the app after that date constitutes acceptance of the revised policy."
    ),
    PolicySection("11. Contact Us",
        "If you have any questions, requests, or complaints regarding this Privacy Policy, please contact us:\n\nSmartRound Clinic Limited\nEmail: privacy@smartroundclinic.co.ke\nPhone: +254 700 000 000\nNairobi, Kenya"
    ),
)

@Composable
internal fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0)) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center).padding(vertical = 12.dp),
                        )
                    }
                    Text(
                        text = "How we handle your data",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .navigationBarsPadding(),
            ) {
                policySections.forEach { section ->
                    Text(
                        text = section.title,
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
