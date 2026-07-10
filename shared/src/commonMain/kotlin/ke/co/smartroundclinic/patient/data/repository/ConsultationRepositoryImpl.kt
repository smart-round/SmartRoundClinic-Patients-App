package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationFileUploadResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationMessageData
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConversationThreadMessagesResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConversationThreadsResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.JoinCallResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.CallJoinInfo
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConversationThread
import ke.co.smartroundclinic.patient.domain.model.MergedHistoryPage
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ConsultationRepositoryImpl(private val client: HttpClient) : ConsultationRepository {

    override suspend fun joinCall(otherUserId: String): Resource<CallJoinInfo> = withContext(Dispatchers.IO) {
        try {
            val res = client.post("chat/$otherUserId/call/join").body<JoinCallResponse>()
            if (res.status && res.data != null) Resource.Success(res.data.toDomain(), res.message)
            else Resource.Error(res.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to join call")
        }
    }

    override suspend fun uploadFile(
        otherUserId: String,
        fileName: String,
        contentType: String,
        bytes: ByteArray,
    ): Resource<ConsultationMessage> = withContext(Dispatchers.IO) {
        try {
            val res = client.post("chat/$otherUserId/files") {
                setBody(MultiPartFormDataContent(formData {
                    append(
                        key = "file",
                        value = bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"$fileName\"")
                            append(HttpHeaders.ContentType, contentType)
                        },
                    )
                }))
            }.body<ConsultationFileUploadResponse>()
            if (res.status && res.data != null) {
                Resource.Success(res.data.toDomain(), res.message)
            } else Resource.Error(res.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload file")
        }
    }

    override suspend fun listThreads(): Resource<List<ConversationThread>> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("consultation/threads").body<ConversationThreadsResponse>()
            if (response.status) {
                Resource.Success(response.data?.map { it.toDomain() } ?: emptyList(), response.message)
            } else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load conversations")
        }
    }

    override suspend fun getMergedMessages(
        doctorId: String,
        patientId: String,
        before: String?,
        size: Int,
    ): Resource<MergedHistoryPage> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("consultation/threads/$doctorId/$patientId/messages") {
                if (before != null) parameter("before", before)
                parameter("size", size)
            }.body<ConversationThreadMessagesResponse>()
            if (response.status) {
                val page = MergedHistoryPage(
                    items = response.data?.items?.map(ConsultationMessageData::toDomain) ?: emptyList(),
                    nextCursor = response.data?.nextCursor,
                    counterpartLastReadAt = response.data?.counterpartLastReadAt,
                    counterpartLastDeliveredAt = response.data?.counterpartLastDeliveredAt,
                )
                Resource.Success(page, response.message)
            } else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load conversation")
        }
    }

    override suspend fun deleteThread(doctorId: String, patientId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            client.delete("consultation/threads/$doctorId/$patientId")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete conversation")
        }
    }
}
