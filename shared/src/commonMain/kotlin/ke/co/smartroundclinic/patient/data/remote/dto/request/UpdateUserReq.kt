package ke.co.smartroundclinic.patient.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserReq(
    val fullName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
)
