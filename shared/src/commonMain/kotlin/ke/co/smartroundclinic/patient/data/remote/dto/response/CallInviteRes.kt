package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.CallInvite
import kotlinx.serialization.Serializable

@Serializable
data class CallInviteRes(
    val data: CallInviteData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class CallInviteData(
    val callId: String,
    val ringTimeoutSeconds: Long,
)

@Serializable
data class CallActionRes(
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

fun CallInviteData.toDomain() = CallInvite(callId = callId, ringTimeoutSeconds = ringTimeoutSeconds)
