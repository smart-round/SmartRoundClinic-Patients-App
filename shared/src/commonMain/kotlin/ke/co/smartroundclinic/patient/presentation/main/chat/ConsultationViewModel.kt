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
import ke.co.smartroundclinic.patient.domain.model.ConversationThread
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository
import ke.co.smartroundclinic.patient.domain.repository.DoctorLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.UserLocalRepository
import ke.co.smartroundclinic.patient.domain.model.CallJoinInfo
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetMyAppointmentsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.GetMergedConsultationHistoryUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.JoinConsultationCallUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.ListConversationThreadsUseCase
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

// Keep the initial/each older-page fetch small — the full history loads incrementally as the
// patient scrolls up, rather than pulling an entire multi-consultation thread up front.
private const val HISTORY_PAGE_SIZE = 5

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
    private val joinCallUseCase: JoinConsultationCallUseCase,
    private val getMyAppointments: GetMyAppointmentsUseCase,
    private val userLocalRepository: UserLocalRepository,
    private val doctorLocalRepository: DoctorLocalRepository,
    private val httpClient: HttpClient,
    private val snackbarController: SnackbarController,
    private val listConversationThreadsUseCase: ListConversationThreadsUseCase,
    private val getMergedHistoryUseCase: GetMergedConsultationHistoryUseCase,
) : ViewModel() {

    // ─── Consultation list ─────────────────────────────────────────────────

    var appointments by mutableStateOf<List<Appointment>>(emptyList())
        private set
    var isLoadingAppointments by mutableStateOf(false)
        private set

    var threads by mutableStateOf<List<ConversationThread>>(emptyList())
        private set
    var isLoadingThreads by mutableStateOf(false)
        private set

    private var doctorCache by mutableStateOf<List<DoctorEntity>>(emptyList())

    var currentUserId by mutableStateOf("")
        private set
    var currentUserProfilePicture by mutableStateOf<String?>(null)
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

    var isLoadingHistory by mutableStateOf(false)
        private set
    var isLoadingMoreHistory by mutableStateOf(false)
        private set
    var hasMoreHistory by mutableStateOf(false)
        private set
    private var nextHistoryCursor: String? = null

    // Derived from pendingFiles — true when any non-failed upload is in progress
    val isUploadingFile: Boolean get() = pendingFiles.any { !it.failed }

    // Call join state — set when the user enters CallScreen and clears when they leave
    var callJoinState by mutableStateOf<Resource<CallJoinInfo>?>(null)
        private set

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
        loadThreads()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userLocalRepository.observeUser().collect { user ->
                currentUserId = user?.id ?: ""
                currentUserProfilePicture = user?.profilePicture
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
                    connectWebSocket(session.id)
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to start consultation", isError = true)
                else -> {}
            }
            isStartingSession = false
        }
    }

    fun loadThreads() {
        viewModelScope.launch {
            isLoadingThreads = true
            when (val result = listConversationThreadsUseCase()) {
                is Resource.Success -> threads = result.data ?: emptyList()
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to load conversations", isError = true)
                else -> {}
            }
            isLoadingThreads = false
        }
    }

    // Bumped on every loadMergedHistory call so a slow, superseded loadMoreHistory (or an old
    // loadMergedHistory itself) can detect it's stale and discard its result instead of mutating
    // `messages` out from under a newer load — this, plus the id dedup below, is what prevents the
    // duplicate-key LazyColumn crash under rapid scroll.
    private var historyGeneration = 0

    /** Loads the merged history for a doctor-patient pair — replaces whatever is currently shown. */
    fun loadMergedHistory(doctorId: String, patientId: String) {
        val generation = ++historyGeneration
        isLoadingHistory = true
        messages.clear()
        nextHistoryCursor = null
        hasMoreHistory = false
        viewModelScope.launch {
            try {
                when (val result = getMergedHistoryUseCase(doctorId, patientId, size = HISTORY_PAGE_SIZE)) {
                    is Resource.Success -> {
                        if (generation != historyGeneration) return@launch
                        val (items, cursor) = result.data ?: (emptyList<ConsultationMessage>() to null)
                        // Backend returns newest-first (for cursor paging); render ascending, oldest at top.
                        messages.addAll(items.asReversed())
                        nextHistoryCursor = cursor
                        hasMoreHistory = cursor != null
                    }
                    is Resource.Error -> snackbarController.show(result.message ?: "Failed to load conversation", isError = true)
                    else -> {}
                }
            } finally {
                if (generation == historyGeneration) isLoadingHistory = false
            }
        }
    }

    /** Loads the next (older) page of history and prepends it. No-op if there's nothing more or a load is already in flight. */
    fun loadMoreHistory(doctorId: String, patientId: String) {
        val cursor = nextHistoryCursor ?: return
        if (isLoadingMoreHistory) return
        // Set synchronously (not inside the coroutine) — otherwise a fast fling can fire this
        // multiple times before the first launch even starts, each fetching and inserting the
        // same page and producing a duplicate message id, which crashes the LazyColumn.
        isLoadingMoreHistory = true
        val generation = historyGeneration
        viewModelScope.launch {
            try {
                when (val result = getMergedHistoryUseCase(doctorId, patientId, before = cursor, size = HISTORY_PAGE_SIZE)) {
                    is Resource.Success -> {
                        if (generation == historyGeneration) {
                            val (items, nextCursor) = result.data ?: (emptyList<ConsultationMessage>() to null)
                            val existingIds = messages.mapTo(HashSet()) { it.id }
                            messages.addAll(0, items.asReversed().filterNot { it.id in existingIds })
                            nextHistoryCursor = nextCursor
                            hasMoreHistory = nextCursor != null
                        }
                    }
                    is Resource.Error -> snackbarController.show(result.message ?: "Failed to load more messages", isError = true)
                    else -> {}
                }
            } finally {
                isLoadingMoreHistory = false
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
        val session = wsSession
        if (session == null) {
            snackbarController.show("Not connected. Please wait a moment and try again.", isError = true)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                session.send(
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

    fun joinCall(sessionId: String) {
        if (callJoinState is Resource.Success) return
        viewModelScope.launch {
            callJoinState = Resource.Loading()
            callJoinState = joinCallUseCase(sessionId)
        }
    }

    fun clearCallState() {
        callJoinState = null
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
        nextHistoryCursor = null
        hasMoreHistory = false
        loadThreads() // refresh list previews now that this thread may have new messages
    }

    override fun onCleared() {
        super.onCleared()
        wsJob?.cancel()
    }
}
