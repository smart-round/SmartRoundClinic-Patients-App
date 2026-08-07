package ke.co.smartroundclinic.patient.core.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ke.co.smartroundclinic.patient.common.Constants.FAQ_URL

@Composable
fun FaqScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WebPageScreen(title = "FAQs", url = FAQ_URL, onBack = onBack, modifier = modifier)
}
