package ke.co.smartroundclinic.patient.presentation.main.chat.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object ConsultationList : NavKey

// patientId is always the currently-signed-in patient (this is the patient app), so it isn't
// carried here — doctorId identifies the permanent thread. latestAppointmentId is only used to
// enrich the header (fallback profile picture) from the appointments list.
@Serializable data class ConsultationChat(val doctorId: String, val doctorName: String, val latestAppointmentId: String) : NavKey
// callId identifies the invite this join is answering/completing — required by the backend's
// atomic-join gate (POST .../call/join now rejects a join with no live invite behind it).
@Serializable data class ConsultationCall(val otherUserId: String, val isVideo: Boolean, val callId: String) : NavKey

// Ringing screen shown to the caller between InviteToCallUseCase and the callee answering —
// see OutgoingCallState. otherUserId doubles as the doctorId (patient app is patient-only).
@Serializable data class OutgoingCall(val otherUserId: String, val calleeName: String, val isVideo: Boolean, val calleePicture: String? = null) : NavKey
