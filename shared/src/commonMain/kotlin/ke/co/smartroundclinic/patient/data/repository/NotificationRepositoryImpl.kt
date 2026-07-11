package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetNotificationsRes
import ke.co.smartroundclinic.patient.domain.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
private data class RegisterDeviceTokenRequest(val deviceToken: String, val platform: String, val tokenType: String)

class NotificationRepositoryImpl(private val client: HttpClient) : NotificationRepository {

    override suspend fun registerDeviceToken(token: String, platform: String, tokenType: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                client.post("notification/device-token") {
                    setBody(RegisterDeviceTokenRequest(deviceToken = token, platform = platform.uppercase(), tokenType = tokenType.uppercase()))
                }
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to register device token")
            }
        }

    override suspend fun getMyNotifications(page: Int, size: Int): Resource<GetNotificationsRes> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.get("notification/my") {
                    parameter("page", page)
                    parameter("size", size)
                }.body<GetNotificationsRes>()
                if (res.status) Resource.Success(res, res.message)
                else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load notifications")
            }
        }

    override suspend fun markAsRead(id: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                client.patch("notification/read") { parameter("id", id) }
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to mark notification as read")
            }
        }
}
