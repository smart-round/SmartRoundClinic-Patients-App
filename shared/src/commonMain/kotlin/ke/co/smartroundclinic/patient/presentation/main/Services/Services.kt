package ke.co.smartroundclinic.patient.presentation.main.Services

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AppointmentsRoot(
    modifier: Modifier = Modifier,
    onAtRootChanged: (Boolean) -> Unit = {},
) {
    onAtRootChanged(true)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Services", style = MaterialTheme.typography.titleLarge)
    }
}
