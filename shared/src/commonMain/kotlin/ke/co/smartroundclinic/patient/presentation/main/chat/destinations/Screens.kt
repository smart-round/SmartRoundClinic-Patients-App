package ke.co.smartroundclinic.patient.presentation.main.chat.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object ConsultationList : NavKey
@Serializable data class ConsultationChat(val appointmentId: String, val doctorName: String) : NavKey
@Serializable data class ConsultationCall(val sessionId: String, val isVideo: Boolean) : NavKey
