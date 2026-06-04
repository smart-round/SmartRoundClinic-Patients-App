package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.IssueCategory
import ke.co.smartroundclinic.patient.domain.model.SupportChatFile
import ke.co.smartroundclinic.patient.domain.model.SupportChatMessage
import ke.co.smartroundclinic.patient.domain.model.SupportTicket
import kotlinx.serialization.Serializable

@Serializable
data class GetMyTicketsRes(
    val httpStatusCode: Int,
    val status: Boolean,
    val message: String,
    val data: TicketPageData? = null,
)

@Serializable
data class GetTicketRes(
    val httpStatusCode: Int,
    val status: Boolean,
    val message: String,
    val data: TicketItemRes? = null,
)

@Serializable
data class TicketPageData(
    val items: List<TicketItemRes>,
    val total: Long = 0,
    val page: Int = 1,
    val size: Int = 20,
    val pages: Long = 0,
)

@Serializable
data class TicketItemRes(
    val id: String,
    val ticketNumber: String,
    val issueCategoryId: String,
    val categoryName: String,
    val title: String,
    val description: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String? = null,
)

fun TicketItemRes.toDomain() = SupportTicket(
    id = id,
    ticketNumber = ticketNumber,
    issueCategoryId = issueCategoryId,
    categoryName = categoryName,
    title = title,
    description = description,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

@Serializable
data class GetIssueCategoriesRes(
    val httpStatusCode: Int,
    val status: Boolean,
    val message: String,
    val data: IssueCategoryPageData? = null,
)

@Serializable
data class IssueCategoryPageData(
    val items: List<IssueCategoryItemRes>,
    val total: Long = 0,
    val page: Int = 1,
    val size: Int = 50,
    val pages: Long = 0,
)

@Serializable
data class IssueCategoryItemRes(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: Boolean = true,
    val createdAt: String,
    val updatedAt: String? = null,
)

fun IssueCategoryItemRes.toDomain() = IssueCategory(id = id, name = name, description = description)

@Serializable
data class SupportChatMessageRes(
    val id: String,
    val ticketId: String,
    val senderId: String,
    val senderName: String,
    val messageType: String,
    val message: String? = null,
    val files: List<SupportChatFileRes> = emptyList(),
    val createdAt: String,
    val updatedAt: String? = null,
)

@Serializable
data class SupportChatFileRes(
    val fileName: String,
    val url: String,
    val contentType: String,
)

fun SupportChatMessageRes.toDomain() = SupportChatMessage(
    id = id,
    ticketId = ticketId,
    senderId = senderId,
    senderName = senderName,
    messageType = messageType,
    message = message,
    files = files.map { SupportChatFile(it.fileName, it.url, it.contentType) },
    createdAt = createdAt,
)

@Serializable
data class GetChatHistoryRes(
    val httpStatusCode: Int,
    val status: Boolean,
    val message: String,
    val data: List<SupportChatMessageRes>? = null,
)

@Serializable
data class UploadChatFileRes(
    val httpStatusCode: Int,
    val status: Boolean,
    val message: String,
    val data: SupportChatMessageRes? = null,
)
