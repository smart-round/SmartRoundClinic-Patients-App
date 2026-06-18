package ke.co.smartroundclinic.patient

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.presentation.navigation.NavigationRoot
import ke.co.smartroundclinic.patient.presentation.theme.SmartRoundTheme
import ke.co.smartroundclinic.patient.presentation.theme.SnackbarError
import ke.co.smartroundclinic.patient.presentation.theme.SnackbarSuccess
import org.koin.compose.koinInject

@Composable
fun App() {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarController: SnackbarController = koinInject()
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarController) {
        snackbarController.messages.collect { event ->
            isError = event.isError
            snackbarHostState.showSnackbar(event.message)
        }
    }

    SmartRoundTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            NavigationRoot(modifier = Modifier.fillMaxSize())
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding(),
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (isError) SnackbarError else SnackbarSuccess,
                        contentColor = Color.White,
                        actionColor = Color.White,
                    )
                },
            )
        }
    }
}
