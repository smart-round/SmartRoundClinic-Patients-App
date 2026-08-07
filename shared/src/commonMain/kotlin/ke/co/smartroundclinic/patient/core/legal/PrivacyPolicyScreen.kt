package ke.co.smartroundclinic.patient.core.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ke.co.smartroundclinic.patient.common.Constants.PRIVACY_POLICY_URL

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WebPageScreen(title = "Privacy Policy", url = PRIVACY_POLICY_URL, onBack = onBack, modifier = modifier)
}
