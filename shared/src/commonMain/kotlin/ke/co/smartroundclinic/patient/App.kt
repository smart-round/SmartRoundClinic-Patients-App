package ke.co.smartroundclinic.patient

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ke.co.smartroundclinic.patient.core.presence.PresenceService
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

    // Presence is a live WebSocket ping loop — closing it the instant the app is backgrounded
    // (rather than waiting for the connection to eventually drop or the server-side TTL to
    // expire) is what makes "online"/"last seen" flip to offline immediately for the other
    // party, instead of up to ~35s late.
    val presenceService: PresenceService = koinInject()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, presenceService) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> presenceService.stop()
                Lifecycle.Event.ON_START -> presenceService.start()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
