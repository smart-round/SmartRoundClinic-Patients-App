package ke.co.smartroundclinic.patient

import androidx.compose.ui.window.ComposeUIViewController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import ke.co.smartroundclinic.patient.koin.initKoin

fun MainViewController() = ComposeUIViewController { App() }

fun doInitKoin() = initKoin()

fun debugBuild() {
    Napier.base(DebugAntilog())
}
