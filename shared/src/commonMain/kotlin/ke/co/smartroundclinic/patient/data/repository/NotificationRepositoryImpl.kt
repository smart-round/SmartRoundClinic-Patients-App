package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
private data class RegisterDeviceTokenRequest(val deviceToken: String, val platform: String)

class NotificationRepositoryImpl(private val client: HttpClient) : NotificationRepository {
    override suspend fun registerDeviceToken(token: String, platform: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                client.post("notification/device-token") {
                    setBody(RegisterDeviceTokenRequest(deviceToken = token, platform = platform.uppercase()))
                }
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to register device token")
            }
        }
}
