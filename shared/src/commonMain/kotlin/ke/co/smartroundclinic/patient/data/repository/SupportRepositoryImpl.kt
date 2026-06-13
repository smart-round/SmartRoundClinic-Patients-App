package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.CreateTicketReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetChatHistoryRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetIssueCategoriesRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetMyTicketsRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetTicketRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.UploadChatFileRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.IssueCategory
import ke.co.smartroundclinic.patient.domain.model.SupportChatMessage
import ke.co.smartroundclinic.patient.domain.model.SupportTicket
import ke.co.smartroundclinic.patient.domain.repository.SupportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SupportRepositoryImpl(private val client: HttpClient) : SupportRepository {

    override suspend fun getCategories(): Resource<List<IssueCategory>> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.get("support/issue-categories/all") {
                    parameter("page", 1)
                    parameter("size", 50)
                }.body<GetIssueCategoriesRes>()
                if (res.status) Resource.Success(res.data?.items?.map { it.toDomain() } ?: emptyList(), res.message)
                else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load categories")
            }
        }

    override suspend fun createTicket(
        categoryId: String,
        title: String,
        description: String,
        complainantName: String,
        complainantEmail: String,
    ): Resource<SupportTicket> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.post("support/tickets") {
                    setBody(CreateTicketReq(categoryId, title, description, complainantName, complainantEmail))
                }.body<GetTicketRes>()
                if (res.status && res.data != null) Resource.Success(res.data.toDomain(), res.message)
                else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to create ticket")
            }
        }

    override suspend fun getMyTickets(page: Int, size: Int): Resource<GetMyTicketsRes> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.get("support/tickets/mine") {
                    parameter("page", page)
                    parameter("size", size)
                }.body<GetMyTicketsRes>()
                if (res.status) Resource.Success(res, res.message)
                else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load tickets")
            }
        }

    override suspend fun getChatHistory(ticketId: String): Resource<List<SupportChatMessage>> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.get("support/tickets/$ticketId/chat").body<GetChatHistoryRes>()
                if (res.status) Resource.Success(res.data?.map { it.toDomain() } ?: emptyList(), res.message)
                else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load chat history")
            }
        }

    override suspend fun uploadChatFile(
        ticketId: String,
        bytes: ByteArray,
        fileName: String,
        contentType: String,
    ): Resource<SupportChatMessage> =
        withContext(Dispatchers.IO) {
            try {
                val res = client.post("support/tickets/$ticketId/chat/files") {
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append(
                                    key = "file",
                                    value = bytes,
                                    headers = Headers.build {
                                        append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"$fileName\"")
                                        append(HttpHeaders.ContentType, contentType)
                                    },
                                )
                            }
                        )
                    )
                }.body<UploadChatFileRes>()
                if (res.status && res.data != null) Resource.Success(res.data.toDomain(), res.message)
                else Resource.Error(res.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to upload file")
            }
        }
}
