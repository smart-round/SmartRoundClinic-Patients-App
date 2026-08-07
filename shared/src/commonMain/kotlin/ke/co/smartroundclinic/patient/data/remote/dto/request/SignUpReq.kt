package ke.co.smartroundclinic.patient.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SignUpReq(
    val fullName: String,
    val email: String,
    val password: String,
)
