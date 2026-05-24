package ke.co.smartroundclinic.patient.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class BookAppointmentReq(
    val doctorId: String,
    val date: String,
    val slotStart: String,
    val notes: String? = null,
)
