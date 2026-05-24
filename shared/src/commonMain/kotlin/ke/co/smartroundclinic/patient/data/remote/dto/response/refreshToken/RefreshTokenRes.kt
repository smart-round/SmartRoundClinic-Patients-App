package ke.co.smartroundclinic.patient.data.remote.dto.response.refreshToken

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RefreshTokenRes(
    val data: RefreshTokenData,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class RefreshTokenData(
    val accessToken: String,
    val accountStatus: String,
    val permissions: List<JsonElement>,
    val policyGroupIds: List<JsonElement>,
    val refreshToken: String,
    val verificationStatus: String,
)
