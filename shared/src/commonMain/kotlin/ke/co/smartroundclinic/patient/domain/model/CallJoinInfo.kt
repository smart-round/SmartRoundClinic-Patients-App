package ke.co.smartroundclinic.patient.domain.model

data class CallJoinInfo(
    val meetingId: String,
    val participantId: String,
    val authToken: String,
    val presetName: String,
)
