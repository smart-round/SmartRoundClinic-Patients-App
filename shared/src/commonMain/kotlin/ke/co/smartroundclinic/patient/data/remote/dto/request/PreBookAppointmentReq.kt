package ke.co.smartroundclinic.patient.data.remote.dto.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PreBookAppointmentReq(
    val doctorId: String,
    val previousAppointmentId: String? = null,
)