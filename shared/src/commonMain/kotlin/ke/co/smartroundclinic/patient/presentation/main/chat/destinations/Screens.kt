package ke.co.smartroundclinic.patient.presentation.main.chat.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object ConsultationList : NavKey

// patientId is always the currently-signed-in patient (this is the patient app), so it isn't
// carried here — doctorId identifies the thread. latestAppointmentId seeds the active session.
@Serializable data class ConsultationChat(val doctorId: String, val doctorName: String, val latestAppointmentId: String) : NavKey
@Serializable data class ConsultationCall(val sessionId: String, val isVideo: Boolean) : NavKey
