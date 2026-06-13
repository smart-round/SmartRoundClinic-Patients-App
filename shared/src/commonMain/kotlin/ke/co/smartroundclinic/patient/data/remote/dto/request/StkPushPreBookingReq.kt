package ke.co.smartroundclinic.patient.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class StkPushPreBookingReq(
    val doctorId: String,
    val phoneNumber: String,
    val previousAppointmentId: String? = null,
)
