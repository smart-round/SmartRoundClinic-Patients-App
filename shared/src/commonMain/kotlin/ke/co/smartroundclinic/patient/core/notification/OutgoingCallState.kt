package ke.co.smartroundclinic.patient.core.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Observable state for a call *this device* just placed (see InviteToCallUseCase) — mirrors [IncomingCallState] but for the caller side. */
sealed class OutgoingCallStatus {
    data class Calling(val callId: String, val otherUserId: String, val calleeName: String?, val isVideo: Boolean) : OutgoingCallStatus()
    data class Answered(val callId: String) : OutgoingCallStatus()
    data class Declined(val callId: String) : OutgoingCallStatus()
}

object OutgoingCallState {
    private val _current = MutableStateFlow<OutgoingCallStatus?>(null)
    val current: StateFlow<OutgoingCallStatus?> = _current

    fun calling(callId: String, otherUserId: String, calleeName: String?, isVideo: Boolean) {
        _current.value = OutgoingCallStatus.Calling(callId, otherUserId, calleeName, isVideo)
    }

    /** No-ops if a newer/different call has already replaced this one. */
    fun answered(callId: String) {
        if (currentCallId() == callId) _current.value = OutgoingCallStatus.Answered(callId)
    }

    fun declined(callId: String) {
        if (currentCallId() == callId) _current.value = OutgoingCallStatus.Declined(callId)
    }

    fun clear() {
        _current.value = null
    }

    private fun currentCallId(): String? = when (val status = _current.value) {
        is OutgoingCallStatus.Calling -> status.callId
        is OutgoingCallStatus.Answered -> status.callId
        is OutgoingCallStatus.Declined -> status.callId
        null -> null
    }
}
