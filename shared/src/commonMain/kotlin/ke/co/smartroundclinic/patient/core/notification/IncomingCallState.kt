package ke.co.smartroundclinic.patient.core.notification

import ke.co.smartroundclinic.patient.domain.model.IncomingCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * App-wide observable ringing state, fed by the chat WebSocket (foreground) and by
 * platform push handlers (background/killed, see [IncomingCallHandler]) alike — a ringing
 * screen collects this to know when to show/hide, independent of which channel the
 * invite/answer/decline/cancel signal actually arrived on.
 */
object IncomingCallState {
    private val _current = MutableStateFlow<IncomingCall?>(null)
    val current: StateFlow<IncomingCall?> = _current

    fun ring(call: IncomingCall) {
        _current.value = call
    }

    /** No-ops if a newer call has already replaced this one — avoids racing a stale clear. */
    fun clear(callId: String) {
        if (_current.value?.callId == callId) _current.value = null
    }

    fun clearAll() {
        _current.value = null
    }
}
