package ke.co.smartroundclinic.patient.data.repository

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.datetime.Clock
import kotlinx.io.RawSource
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import ke.co.smartroundclinic.patient.common.Constants
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.CallActionReq
import ke.co.smartroundclinic.patient.data.remote.dto.request.InviteToCallReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.CallActionRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.CallInviteRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.CompleteUploadReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationFileUploadResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.PresignUploadReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.PresignUploadResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationMessageData
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConversationThreadMessagesResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConversationThreadsResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.JoinCallResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.CallInvite
import ke.co.smartroundclinic.patient.domain.model.CallJoinInfo
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConversationThread
import ke.co.smartroundclinic.patient.domain.model.MergedHistoryPage
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ConsultationRepositoryImpl(
    private val client: HttpClient,
    /** Auth-free client for pre-signed storage PUTs — see CoreModule.STORAGE_HTTP_CLIENT. */
    private val storageClient: HttpClient,
) : ConsultationRepository {

    override suspend fun joinCall(otherUserId: String): Resource<CallJoinInfo> = withContext(Dispatchers.IO) {
        // HttpRequestRetry (HttpClientFactory.kt) only retries GET requests — POSTs are excluded
        // there because most of them (invite/decline/cancel) have side effects that aren't safe
        // to retry blindly. Joining a call is idempotent (just hands back the room token), and
        // this call fires the moment an incoming call is answered — often within moments of the
        // process (re)launching from a VoIP push, when the network stack hasn't warmed up yet and
        // the very first attempt can fail fast on a transient exception. Retry locally here.
        var lastError: Exception? = null
        repeat(3) { attempt ->
            try {
                val res = client.post("chat/$otherUserId/call/join").body<JoinCallResponse>()
                return@withContext if (res.status && res.data != null) Resource.Success(res.data.toDomain(), res.message)
                else Resource.Error(res.message)
            } catch (e: Exception) {
                lastError = e
                if (attempt < 2) delay(500L * (attempt + 1))
            }
        }
        Resource.Error(lastError?.message ?: "Failed to join call")
    }

    override suspend fun inviteToCall(otherUserId: String, isVideo: Boolean): Resource<CallInvite> = withContext(Dispatchers.IO) {
        try {
            val res = client.post("chat/$otherUserId/call/invite") {
                setBody(InviteToCallReq(isVideo = isVideo))
            }.body<CallInviteRes>()
            if (res.status && res.data != null) Resource.Success(res.data.toDomain(), res.message)
            else Resource.Error(res.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to start call")
        }
    }

    override suspend fun declineCall(otherUserId: String, callId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = client.post("chat/$otherUserId/call/decline") {
                setBody(CallActionReq(callId = callId))
            }.body<CallActionRes>()
            if (res.status) Resource.Success(Unit, res.message) else Resource.Error(res.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to decline call")
        }
    }

    override suspend fun cancelCall(otherUserId: String, callId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = client.post("chat/$otherUserId/call/cancel") {
                setBody(CallActionReq(callId = callId))
            }.body<CallActionRes>()
            if (res.status) Resource.Success(Unit, res.message) else Resource.Error(res.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to cancel call")
        }
    }

    override suspend fun uploadFile(
        otherUserId: String,
        fileName: String,
        contentType: String,
        sizeBytes: Long,
        openSource: () -> RawSource,
        onProgress: (sent: Long, total: Long) -> Unit,
    ): Resource<ConsultationMessage> = withContext(Dispatchers.IO) {
        val startedAt = Clock.System.now().toEpochMilliseconds()
        fun elapsed() = Clock.System.now().toEpochMilliseconds() - startedAt
        Napier.i(tag = "SRC-UPLOAD", message = "start name=$fileName type=$contentType bytes=$sizeBytes")
        try {
            // 1. Ask the API where to put it. The API also enforces its own size ceiling here,
            //    so an oversized file is rejected before a single byte of it is sent.
            val presign = client.post("chat/$otherUserId/files/presign") {
                setBody(PresignUploadReq(fileName = fileName, contentType = contentType, sizeBytes = sizeBytes))
            }.body<PresignUploadResponse>()
            val target = presign.data
            if (!presign.status || target == null) {
                Napier.e(tag = "SRC-UPLOAD", message = "presign refused after ${elapsed()}ms: ${presign.message}")
                return@withContext Resource.Error(presign.message)
            }

            // 2. Send the bytes straight to storage. Uses the auth-free client — a Bearer token
            //    on a pre-signed URL is a signature mismatch — and the Content-Type must match
            //    exactly what the server signed.
            val putResponse = storageClient.put(target.uploadUrl) {
                timeout {
                    requestTimeoutMillis = Constants.UPLOAD_REQUEST_TIMEOUT_MS
                    socketTimeoutMillis = Constants.UPLOAD_SOCKET_TIMEOUT_MS
                }
                contentType(ContentType.parse(target.contentType))
                // Streamed in fixed chunks straight off disk — the file is never materialised
                // in memory, which is what makes a 300MB attachment possible at all.
                setBody(
                    object : OutgoingContent.WriteChannelContent() {
                        override val contentType = ContentType.parse(target.contentType)
                        override val contentLength = sizeBytes
                        override suspend fun writeTo(channel: ByteWriteChannel) {
                            // Reads through RawSource/Buffer rather than the ByteArray overload of
                            // readAtMostTo: on Kotlin/Native that name also matches an internal
                            // CPointer overload, which fails to compile for iOS.
                            val buffer = Buffer()
                            openSource().use { source ->
                                var sent = 0L
                                // Only report on whole-percent changes — a 300MB file is ~4800
                                // chunks, and recomposing the bubble that often would cost more
                                // than the upload.
                                var lastReportedPercent = -1
                                while (true) {
                                    val read = source.readAtMostTo(buffer, Constants.UPLOAD_CHUNK_BYTES.toLong())
                                    if (read <= 0L) break
                                    val bytes = buffer.readByteArray()
                                    channel.writeFully(bytes)
                                    sent += bytes.size
                                    val percent = if (sizeBytes > 0) ((sent * 100) / sizeBytes).toInt() else 0
                                    if (percent != lastReportedPercent) {
                                        if (percent / 10 != lastReportedPercent / 10) {
                                            Napier.i(tag = "SRC-UPLOAD", message = "$percent% ($sent/$sizeBytes) at ${elapsed()}ms")
                                        }
                                        lastReportedPercent = percent
                                        onProgress(sent, sizeBytes)
                                    }
                                }
                            }
                        }
                    },
                )
            }
            if (!putResponse.status.isSuccess()) {
                Napier.e(tag = "SRC-UPLOAD", message = "storage PUT failed after ${elapsed()}ms http=${putResponse.status}")
                return@withContext Resource.Error("Failed to upload file (${putResponse.status.value})")
            }
            Napier.i(tag = "SRC-UPLOAD", message = "stored in ${elapsed()}ms")

            // 3. Record the message now the object exists.
            val res = client.post("chat/$otherUserId/files/complete") {
                setBody(
                    CompleteUploadReq(
                        messageId = target.messageId,
                        key = target.key,
                        fileName = fileName,
                        contentType = target.contentType,
                        sizeBytes = sizeBytes,
                    ),
                )
            }.body<ConsultationFileUploadResponse>()
            Napier.i(tag = "SRC-UPLOAD", message = "completed in ${elapsed()}ms status=${res.status} http=${res.httpStatusCode}")
            if (res.status && res.data != null) {
                Resource.Success(res.data.toDomain(), res.message)
            } else Resource.Error(res.message)
        } catch (e: Exception) {
            Napier.e(tag = "SRC-UPLOAD", message = "failed after ${elapsed()}ms ${e::class.simpleName}: ${e.message}", throwable = e)
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
