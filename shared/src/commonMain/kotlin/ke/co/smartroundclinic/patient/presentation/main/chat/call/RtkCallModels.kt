package ke.co.smartroundclinic.patient.presentation.main.chat.call

sealed class CallConnectionState {
    data object Connecting : CallConnectionState()
    data object Connected : CallConnectionState()
    data class Failed(val message: String) : CallConnectionState()
    data object Ended : CallConnectionState()
}

data class RemoteParticipantInfo(
    val name: String,
    val audioEnabled: Boolean,
    val videoEnabled: Boolean,
)
