package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class GetUserRes(
    val data: UserData,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class UserData(
    val accountStatus: String,
    val createdAt: String,
    val email: String,
    val fullName: String,
    val gender: String,
    val id: String,
    val role: String,
    val verificationStatus: String,
    val profilePicture: String? = null,
    val dateOfBirth: String? = null,
    val phoneNumber: String? = null,
)

fun UserData.toDomain() = User(
    id = id,
    fullName = fullName,
    email = email,
    gender = gender,
    phoneNumber = phoneNumber,
    dateOfBirth = dateOfBirth,
    profilePicture = profilePicture,
    role = role,
    accountStatus = accountStatus,
    verificationStatus = verificationStatus,
    createdAt = createdAt,
)
