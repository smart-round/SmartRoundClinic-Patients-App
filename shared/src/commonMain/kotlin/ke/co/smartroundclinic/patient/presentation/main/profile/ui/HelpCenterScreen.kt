package ke.co.smartroundclinic.patient.presentation.main.profile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.Primary90
import ke.co.smartroundclinic.patient.presentation.theme.ShapeCard

private data class HelpSection(val title: String, val articles: List<Pair<String, String>>)

private val helpSections = listOf(
    HelpSection("Getting Started", listOf(
        "What is SmartRound Clinic?" to
            "SmartRound Clinic is a Kenya-based telemedicine platform that connects patients with licensed doctors for virtual consultations via video or audio call. You can book appointments, receive diagnoses, get prescriptions, and access medical records — all from your phone.",
        "How do I create an account?" to
            "Download the SmartRound Clinic app, tap Sign Up, and enter your full name, phone number, email address, and date of birth. Verify your phone number via OTP and you're ready to book your first consultation.",
        "Is SmartRound Clinic available 24/7?" to
            "Doctor availability varies. Many doctors on our platform offer early morning, evening, and weekend slots. You can always browse the availability calendar when booking to find a time that works for you.",
    )),
    HelpSection("Booking & Appointments", listOf(
        "How do I book an appointment?" to
            "Go to the Services tab, choose a medical speciality or search for a specific doctor. Select an available date and time slot, then tap Pay Now. Once payment is confirmed your booking is secured.",
        "Can I rebook with the same doctor?" to
            "Yes. If you had a consultation within the last 30 days you can book a follow-up at a reduced follow-up fee. Open the completed appointment in the Bookings tab and tap Rebook Follow-Up.",
        "How do I cancel an appointment?" to
            "Go to Bookings, tap the appointment you want to cancel, and select Cancel. Cancellations made more than 24 hours before the scheduled time are eligible for a full refund.",
        "What happens if the doctor is late?" to
            "If a doctor is more than 15 minutes late without notice, you may cancel the appointment for a full refund. Please contact support if this happens so we can assist you promptly.",
    )),
    HelpSection("Payments & Billing", listOf(
        "What payment methods are supported?" to
            "SmartRound Clinic currently supports M-Pesa. Select your slot, tap Pay Now, and complete the M-Pesa prompt on your phone. Payment confirmation is instant.",
        "How do I get a refund?" to
            "Refunds for eligible cancellations are processed within 3–5 business days back to your M-Pesa account. Contact our support team with your booking reference if you haven't received your refund after 5 days.",
        "Where can I see my payment history?" to
            "Go to the Bookings tab and tap the Payments tab at the top. All your transactions — completed and pending — are listed there with receipts.",
    )),
    HelpSection("Consultations & Telemedicine", listOf(
        "How do video consultations work?" to
            "At your appointment time, open the app and navigate to your active consultation in the Chat tab. Tap Join Call to start the video session with your doctor. Make sure you're in a quiet, well-lit place with a stable internet connection.",
        "What should I do before a consultation?" to
            "Prepare a list of your symptoms and how long you've had them. Have any relevant medical history, current medications, or test results on hand. A good internet connection (Wi-Fi preferred) ensures the best call quality.",
        "Can the doctor prescribe medication?" to
            "Yes. Licensed doctors on SmartRound Clinic can issue digital prescriptions. Your prescription will be shared in the consultation chat for you to take to any pharmacy.",
        "Is my consultation private and secure?" to
            "All consultations are encrypted end-to-end. Your medical records and conversation history are stored securely and are only accessible to you and your attending doctor.",
    )),
    HelpSection("Account & Profile", listOf(
        "How do I update my personal information?" to
            "Go to Profile → Personal Information. You can update your name, phone number, date of birth, gender, and profile photo.",
        "How do I change my password?" to
            "Go to Profile → Reset Password. You'll receive a verification code on your email to confirm the change.",
        "How do I delete my account?" to
            "Please contact our support team via the Support screen to request account deletion. We'll process your request within 7 working days in accordance with data protection law.",
    )),
)

@Composable
internal fun HelpCenterScreen(
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
                            text = "Help Center",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center).padding(vertical = 12.dp),
                        )
                    }
                    Text(
                        text = "Browse articles and guides",
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
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
            ) {
                helpSections.forEach { section ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(8.dp).clip(CircleShape).background(Primary40),
                        )
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.surface, shape = ShapeCard),
                    ) {
                        section.articles.forEachIndexed { index, (question, answer) ->
                            HelpArticleItem(question = question, answer = answer)
                            if (index < section.articles.lastIndex) {
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun HelpArticleItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { expanded = !expanded },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Text(
                text = answer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
            )
        }
    }
}
