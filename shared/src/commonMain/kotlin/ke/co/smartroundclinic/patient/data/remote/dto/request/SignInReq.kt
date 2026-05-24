package ke.co.smartroundclinic.patient.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SignInReq(
    val email: String,
    val password: String,
)
