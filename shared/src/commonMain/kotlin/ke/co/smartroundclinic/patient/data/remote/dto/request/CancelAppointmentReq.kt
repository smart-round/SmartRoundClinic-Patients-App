package ke.co.smartroundclinic.patient.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CancelAppointmentReq(
    val reason: String? = null,
)
