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
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationFileUploadResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationMessageData
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationMessagesResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationSessionResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationWsOutgoing
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConsultationSession
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class StartConsultationReq(val appointmentId: String)

class ConsultationRepositoryImpl(private val client: HttpClient) : ConsultationRepository {

    override suspend fun startOrGet(appointmentId: String): Resource<ConsultationSession> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.post("consultation") {
                    setBody(StartConsultationReq(appointmentId = appointmentId))
                }.body<ConsultationSessionResponse>()
                if (response.status) {
                    val data = response.data?.toDomain()
                        ?: return@withContext Resource.Error("No session data")
                    Resource.Success(data, response.message)
                } else {
                    Resource.Error(response.message)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to start consultation")
            }
        }

    override suspend fun getMessages(
        sessionId: String,
        page: Int,
        size: Int,
    ): Resource<List<ConsultationMessage>> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("consultation/$sessionId/messages") {
                parameter("page", page)
                parameter("size", size)
            }.body<ConsultationMessagesResponse>()
            if (response.status) {
                Resource.Success(
                    response.data?.items?.map(ConsultationMessageData::toDomain) ?: emptyList(),
                    response.message,
                )
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load messages")
        }
    }

    override suspend fun uploadFile(
        sessionId: String,
        fileName: String,
        contentType: String,
        bytes: ByteArray,
    ): Resource<ConsultationMessage> = withContext(Dispatchers.IO) {
        try {
            val res = client.post("consultation/$sessionId/files") {
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
}
