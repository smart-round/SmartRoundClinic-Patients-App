package ke.co.smartroundclinic.patient.core.snackbar

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class SnackbarEvent(val message: String, val isError: Boolean = false)

class SnackbarController {
    private val _messages = MutableSharedFlow<SnackbarEvent>(extraBufferCapacity = 16)
    val messages = _messages.asSharedFlow()

    fun show(message: String, isError: Boolean = false) {
        _messages.tryEmit(SnackbarEvent(message, isError))
    }
}
