package ke.co.smartroundclinic.patient.presentation.main.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import ke.co.smartroundclinic.patient.common.Constants
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.database.entity.DoctorEntity
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationMessageData
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationWsOutgoing
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConsultationSession
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository
import ke.co.smartroundclinic.patient.domain.repository.DoctorLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.UserLocalRepository
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetMyAppointmentsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.GetConsultationMessagesUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.StartConsultationUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private val wsJson = Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }

data class PendingFile(
    val localId: String,
    val fileName: String,
    val contentType: String,
    val bytes: ByteArray,
    val failed: Boolean = false,
) {
    override fun equals(other: Any?) = other is PendingFile && localId == other.localId
    override fun hashCode() = localId.hashCode()
}

class ConsultationViewModel(
    private val consultationRepository: ConsultationRepository,
    private val startConsultationUseCase: StartConsultationUseCase,
    private val getMessagesUseCase: GetConsultationMessagesUseCase,
    private val getMyAppointments: GetMyAppointmentsUseCase,
    private val userLocalRepository: UserLocalRepository,
    private val doctorLocalRepository: DoctorLocalRepository,
    private val httpClient: HttpClient,
    private val snackbarController: SnackbarController,
) : ViewModel() {

    // ─── Consultation list ─────────────────────────────────────────────────

    var appointments by mutableStateOf<List<Appointment>>(emptyList())
        private set
    var isLoadingAppointments by mutableStateOf(false)
        private set

    private var doctorCache by mutableStateOf<List<DoctorEntity>>(emptyList())

    var currentUserId by mutableStateOf("")
        private set

    // ─── Active session ────────────────────────────────────────────────────

    var activeSession by mutableStateOf<ConsultationSession?>(null)
        private set
    var isStartingSession by mutableStateOf(false)
        private set

    val messages = mutableStateListOf<ConsultationMessage>()
    val pendingFiles = mutableStateListOf<PendingFile>()
    var isConnected by mutableStateOf(false)
        private set

    // Derived from pendingFiles — true when any non-failed upload is in progress
    val isUploadingFile: Boolean get() = pendingFiles.any { !it.failed }

    private var wsJob: Job? = null
    private var wsSession: DefaultWebSocketSession? = null

    init {
        loadCurrentUser()
        loadDoctors()
        // Show cached appointments immediately, then refresh from network
        viewModelScope.launch {
            val cached = getMyAppointments.getCached().filterConsultable().sortedByDescending { it.date }
            if (cached.isNotEmpty()) appointments = cached
            loadAppointments()
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userLocalRepository.observeUser().collect { user ->
                currentUserId = user?.id ?: ""
            }
        }
    }

    private fun loadDoctors() {
        viewModelScope.launch {
            doctorCache = try { doctorLocalRepository.getDoctors() } catch (_: Exception) { emptyList() }
        }
    }

    fun loadAppointments() {
        viewModelScope.launch {
            isLoadingAppointments = true
            when (val result = getMyAppointments()) {
                is Resource.Success -> appointments = (result.data ?: emptyList())
                    .filterConsultable()
                    .sortedByDescending { it.date }
                is Resource.Error -> if (appointments.isEmpty()) {
                    snackbarController.show(result.message ?: "Failed to load", isError = true)
                }
                else -> {}
            }
            isLoadingAppointments = false
        }
    }

    private fun List<Appointment>.filterConsultable() = filter {
        it.status.equals("CONFIRMED", ignoreCase = true) ||
        it.status.equals("COMPLETED", ignoreCase = true)
    }

    fun doctorName(doctorId: String): String =
        doctorCache.firstOrNull { it.id == doctorId }?.name ?: "Doctor"

    fun doctorPicture(doctorId: String): String? =
        doctorCache.firstOrNull { it.id == doctorId }?.profilePicture

    // ─── Consultation session + WebSocket ──────────────────────────────────

    fun startConsultation(appointmentId: String) {
        if (isStartingSession) return
        viewModelScope.launch {
            isStartingSession = true
            when (val result = startConsultationUseCase(appointmentId)) {
                is Resource.Success -> {
                    val session = result.data ?: return@launch
                    activeSession = session
                    loadHistory(session.id)
                    connectWebSocket(session.id)
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to start consultation", isError = true)
                else -> {}
            }
            isStartingSession = false
        }
    }

    private fun loadHistory(sessionId: String) {
        viewModelScope.launch {
            when (val result = getMessagesUseCase(sessionId)) {
                is Resource.Success -> {
                    messages.clear()
                    messages.addAll(result.data ?: emptyList())
                }
                else -> {}
            }
        }
    }

    private fun connectWebSocket(sessionId: String) {
        wsJob?.cancel()
        isConnected = false
        wsJob = viewModelScope.launch(Dispatchers.IO) {
            val wsBase = Constants.BASE_URL
                .replace("https://", "wss://")
                .replace("http://", "ws://")
            var attempt = 0
            while (isActive) {
                try {
                    httpClient.webSocket("${wsBase}consultation/$sessionId/chat") {
                        wsSession = this
                        withContext(Dispatchers.Main) { isConnected = true }
                        attempt = 0

                        // Ping every 25 s; close the session on failure so the reconnect loop fires
                        launch {
                            while (isActive) {
                                delay(25_000L)
                                try {
                                    send(Frame.Ping(ByteArray(0)))
                                } catch (_: Exception) {
                                    try { close(CloseReason(CloseReason.Codes.GOING_AWAY, "")) } catch (_: Exception) {}
                                    break
                                }
                            }
                        }

                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                try {
                                    val dto = wsJson.decodeFromString<ConsultationMessageData>(frame.readText())
                                    val msg = dto.toDomain()
                                    withContext(Dispatchers.Main) {
                                        if (messages.none { it.id == msg.id }) {
                                            messages.add(msg)
                                            // Remove matching pending once server echoes the upload back
                                            if (msg.messageType.uppercase() == "FILE" && msg.senderId == currentUserId) {
                                                pendingFiles.removeAll { p ->
                                                    msg.files.any { it.fileName == p.fileName }
                                                }
                                            }
                                        }
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                } finally {
                    wsSession = null
                    withContext(Dispatchers.Main) { isConnected = false }
                }

                // Exponential back-off: 1 s, 2 s, 4 s … up to 30 s
                if (isActive) {
                    attempt++
                    delay(minOf(1_000L shl minOf(attempt - 1, 5), 30_000L))
                }
            }
        }
    }

    fun sendText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                wsSession?.send(
                    Frame.Text(wsJson.encodeToString(ConsultationWsOutgoing(type = "TEXT", message = text)))
                )
            } catch (_: Exception) {
                snackbarController.show("Failed to send message", isError = true)
            }
        }
    }

    fun sendFile(fileName: String, contentType: String, bytes: ByteArray) {
        val sessionId = activeSession?.id
        if (sessionId == null) {
            snackbarController.show("No active session", isError = true)
            return
        }
        val pending = PendingFile(
            localId = "p${kotlin.random.Random.nextInt()}",
            fileName = fileName,
            contentType = contentType,
            bytes = bytes,
        )
        pendingFiles.add(pending)

        viewModelScope.launch {
            when (val result = consultationRepository.uploadFile(sessionId, fileName, contentType, bytes)) {
                is Resource.Success -> {
                    // The WebSocket change-stream broadcast will deliver the message
                    // and remove the pending entry. As a fallback, also append the
                    // response message directly if it didn't arrive via the socket.
                    val msg = result.data
                    if (msg != null && messages.none { it.id == msg.id }) {
                        messages.add(msg)
                        pendingFiles.removeAll { it.localId == pending.localId }
                    }
                }
                is Resource.Error -> {
                    snackbarController.show(result.message ?: "Failed to send file", isError = true)
                    markFailed(pending)
                }
                else -> {}
            }
        }
    }

    private fun markFailed(pending: PendingFile) {
        val idx = pendingFiles.indexOfFirst { it.localId == pending.localId }
        if (idx >= 0) pendingFiles[idx] = pending.copy(failed = true)
    }

    fun endConsultation() {
        wsJob?.cancel()
        wsSession = null
        isConnected = false
        activeSession = null
        messages.clear()
        pendingFiles.clear()
    }

    override fun onCleared() {
        super.onCleared()
        wsJob?.cancel()
    }
}
