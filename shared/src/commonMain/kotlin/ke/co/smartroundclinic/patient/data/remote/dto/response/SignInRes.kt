package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.AuthTokens
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SignInRes(
    val data: SignInData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class SignInData(
    val accessToken: String? = null,
    val accountStatus: String,
    val permissions: List<JsonElement>,
    val policyGroupIds: List<JsonElement>,
    val refreshToken: String? = null,
    val verificationStatus: String,
)

fun SignInRes.toDomain() = AuthTokens(
    accessToken = data?.accessToken,
    refreshToken = data?.refreshToken,
    accountStatus = data?.accountStatus,
    verificationStatus = data?.verificationStatus,
)
