package ke.co.smartroundclinic.patient.presentation.main.support

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftric.kvault.KVault
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import ke.co.smartroundclinic.patient.common.Constants
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.common.Resource.Success
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.data.remote.dto.request.WsChatMessageReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.SupportChatMessageRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.SupportChatMessage
import ke.co.smartroundclinic.patient.domain.usecase.support.GetSupportChatHistoryUseCase
import ke.co.smartroundclinic.patient.domain.usecase.support.UploadChatFileUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; isLenient = true }
private const val WS_BASE = "wss://api.smartroundclinic.co.ke/"

class SupportChatViewModel(
    private val ticketId: String,
    private val client: HttpClient,
    private val kvault: KVault,
    private val uploadChatFileUseCase: UploadChatFileUseCase,
    private val getChatHistoryUseCase: GetSupportChatHistoryUseCase,
    private val snackbarController: SnackbarController,
) : ViewModel() {

    var messages by mutableStateOf<List<SupportChatMessage>>(emptyList())
        private set

    var isConnected by mutableStateOf(false)
        private set

    var isUploading by mutableStateOf(false)
        private set

    var isLoadingHistory by mutableStateOf(false)
        private set

    private val pendingMessages = Channel<String>(Channel.BUFFERED)
    private var wsJob: Job? = null

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            isLoadingHistory = true
            val result = getChatHistoryUseCase(ticketId = ticketId)
            if (result is Success) {
                messages = result.data ?: emptyList()
            }
            isLoadingHistory = false
            connect()
        }
    }

    fun connect() {
        wsJob?.cancel()
        wsJob = viewModelScope.launch {
            try {
                val token = kvault.string(Constants.KEY_ACCESS_TOKEN) ?: return@launch
                client.webSocket(
                    urlString = "${WS_BASE}support/tickets/$ticketId/chat",
                    request = { headers.append("Authorization", "Bearer $token") },
                ) {
                    isConnected = true

                    val sendJob = launch {
                        for (text in pendingMessages) {
                            send(Frame.Text(text))
                        }
                    }

                    try {
                        while (true) {
                            val frame = incoming.receive()
                            if (frame is Frame.Text) {
                                runCatching {
                                    val res = json.decodeFromString<SupportChatMessageRes>(frame.readText())
                                    val msg = res.toDomain()
                                    if (messages.none { it.id == msg.id }) {
                                        messages = messages + msg
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) { }

                    sendJob.cancel()
                    isConnected = false
                }
            } catch (_: Exception) {
                isConnected = false
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            pendingMessages.send(json.encodeToString(WsChatMessageReq(message = trimmed)))
        }
    }

    fun uploadFile(bytes: ByteArray, fileName: String, contentType: String) {
        viewModelScope.launch {
            isUploading = true
            val result = uploadChatFileUseCase(ticketId, bytes, fileName, contentType)
            isUploading = false
            when (result) {
                is Resource.Success -> result.data?.let { msg ->
                    if (messages.none { it.id == msg.id }) messages = messages + msg
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to upload file", isError = true)
                else -> Unit
            }
        }
    }

    override fun onCleared() {
        wsJob?.cancel()
        pendingMessages.close()
        super.onCleared()
    }
}
