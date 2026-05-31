package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.Notification
import kotlinx.serialization.Serializable

@Serializable
data class GetNotificationsRes(
    val data: NotificationPageData,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class NotificationPageData(
    val items: List<NotificationItemRes>,
    val total: Long = 0,
    val page: Int = 1,
    val size: Int = 50,
    val pages: Long = 0,
)

@Serializable
data class NotificationItemRes(
    val id: String,
    val title: String,
    val message: String,
    val channel: String,
    val destination: String,
    val recipientId: String? = null,
    val status: String,
    val createdAt: String,
    val readAt: String? = null,
)

fun NotificationItemRes.toDomain() = Notification(
    id = id,
    title = title,
    message = message,
    channel = channel,
    status = status,
    createdAt = createdAt,
    readAt = readAt,
)
