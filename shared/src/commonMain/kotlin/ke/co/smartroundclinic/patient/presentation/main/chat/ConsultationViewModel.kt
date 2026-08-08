package ke.co.smartroundclinic.patient.presentation.main.chat

import ke.co.smartroundclinic.patient.presentation.main.chat.util.formatFileSizeDecimal
import kotlinx.io.RawSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.readText
import ke.co.smartroundclinic.patient.common.Constants
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.database.entity.DoctorEntity
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationCallAnsweredEventData
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationCallCancelledEventData
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationCallDeclinedEventData
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationCallInviteEventData
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationMessageData
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationPresenceEventData
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationTypingEventData
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationWsEventPeek
import ke.co.smartroundclinic.patient.data.remote.dto.response.ConsultationWsOutgoing
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConversationThread
import ke.co.smartroundclinic.patient.domain.model.NextAppointment
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository
import ke.co.smartroundclinic.patient.domain.repository.DoctorLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.UserLocalRepository
import ke.co.smartroundclinic.patient.domain.model.CallJoinInfo
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetMyAppointmentsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetNextAppointmentUseCase
import ke.co.smartroundclinic.patient.core.notification.ActiveCallNotifier
import ke.co.smartroundclinic.patient.core.notification.IncomingCallHandler
import ke.co.smartroundclinic.patient.core.notification.OutgoingCallState
import ke.co.smartroundclinic.patient.domain.usecase.consultation.CancelCallUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.DeleteConversationThreadUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.GetMergedConsultationHistoryUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.InviteToCallUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.JoinConsultationCallUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.ListConversationThreadsUseCase
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
// patient scrolls up, rather than pulling an entire multi-consultation thread up front. Needs to be
// large enough to fill the screen on open (a handful of short bubbles fit easily), otherwise the
// list has no scroll room at all and the user can never trigger the next page.
private const val HISTORY_PAGE_SIZE = 20

/** A rejected attachment clears itself after this long. */
private const val FAILED_ATTACHMENT_AUTO_DISMISS_MS = 5_000L

data class PendingFile(
    val localId: String,
    val fileName: String,
    val contentType: String,
    /**
     * Thumbnail bytes for the in-flight bubble, and *only* that. Null for anything above
     * [Constants.MAX_INLINE_PREVIEW_BYTES] — the upload itself streams from disk, so holding a
     * large attachment here purely to draw a preview would defeat the point and exhaust the heap.
     */
    val previewBytes: ByteArray? = null,
    val failed: Boolean = false,
    /** Shown beneath the attachment when it fails, e.g. the file-too-large message. */
    val errorText: String? = null,
    /** Secondary line under [errorText], e.g. the actual size against the limit. */
    val detailText: String? = null,
    /** Bytes written so far and the total, for the in-flight progress bar. */
    val sentBytes: Long = 0L,
    val totalBytes: Long = 0L,
) {
    /** 0f..1f, or null when the total isn't known yet. */
    val progress: Float? get() = if (totalBytes > 0) (sentBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else null
    // localId alone is NOT enough: the progress fields change while the upload runs, and if
    // equals ignores them Compose treats each update as an identical value and skips
    // recomposition — the percentage then never moves on screen. ByteArray is deliberately
    // still excluded, since its equality is by identity.
    override fun equals(other: Any?) = other is PendingFile &&
        localId == other.localId &&
        sentBytes == other.sentBytes &&
        totalBytes == other.totalBytes &&
        failed == other.failed &&
        errorText == other.errorText

    override fun hashCode() = localId.hashCode()
}

class ConsultationViewModel(
    private val consultationRepository: ConsultationRepository,
    private val joinCallUseCase: JoinConsultationCallUseCase,
    private val inviteToCallUseCase: InviteToCallUseCase,
    private val cancelCallUseCase: CancelCallUseCase,
    private val getMyAppointments: GetMyAppointmentsUseCase,
    private val userLocalRepository: UserLocalRepository,
    private val doctorLocalRepository: DoctorLocalRepository,
    private val httpClient: HttpClient,
    private val snackbarController: SnackbarController,
    private val listConversationThreadsUseCase: ListConversationThreadsUseCase,
    private val getMergedHistoryUseCase: GetMergedConsultationHistoryUseCase,
    private val deleteConversationThreadUseCase: DeleteConversationThreadUseCase,
    private val getNextAppointmentUseCase: GetNextAppointmentUseCase,
) : ViewModel() {

    // ─── Consultation list ─────────────────────────────────────────────────

    var appointments by mutableStateOf<List<Appointment>>(emptyList())
        private set
    var isLoadingAppointments by mutableStateOf(false)
        private set

    var nextAppointment by mutableStateOf<NextAppointment?>(null)
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

    // The doctor id of the permanent thread currently connected over the chat WebSocket.
    private var currentOtherUserId: String? = null

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

    // ─── Typing / presence for the currently open conversation ─────────────
    var otherPartyTyping by mutableStateOf(false)
        private set
    var otherPartyOnline by mutableStateOf(false)
        private set
    var otherPartyLastSeenAt by mutableStateOf<String?>(null)
        private set
    private var typingClearJob: Job? = null
    private var lastTypingSentTrue = false

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

    // Chat threads are permanent and can span many appointments over time — this is the single
    // source of truth (backend-resolved) for whether/when the video-call option should appear,
    // rather than picking an appointment out of the locally cached list.
    fun loadNextAppointment(otherUserId: String) {
        nextAppointment = null
        viewModelScope.launch {
            when (val result = getNextAppointmentUseCase(otherUserId)) {
                is Resource.Success -> nextAppointment = result.data
                else -> Unit
            }
        }
    }

    fun doctorName(doctorId: String): String =
        doctorCache.firstOrNull { it.id == doctorId }?.name ?: "Doctor"

    fun doctorPicture(doctorId: String): String? =
        doctorCache.firstOrNull { it.id == doctorId }?.profilePicture

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

    private var threadsPollJob: Job? = null

    // The chat list's isOnline/lastSeenAt are only as fresh as the last loadThreads() call — unlike
    // an open conversation (which gets live PRESENCE frames over its own socket), the list has no
    // socket of its own. Poll it while it's the visible screen so presence there isn't stale.
    fun startThreadsPolling() {
        if (threadsPollJob?.isActive == true) return
        threadsPollJob = viewModelScope.launch {
            while (isActive) {
                delay(10_000L)
                when (val result = listConversationThreadsUseCase()) {
                    is Resource.Success -> threads = result.data ?: threads
                    else -> {}
                }
            }
        }
    }

    fun stopThreadsPolling() {
        threadsPollJob?.cancel()
        threadsPollJob = null
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
        // Seed presence from the already-fetched thread list — PRESENCE frames on the live socket
        // will keep it fresh from here.
        threads.firstOrNull { it.doctorId == doctorId && it.patientId == patientId }?.let {
            otherPartyOnline = it.isOnline
            otherPartyLastSeenAt = it.lastSeenAt
        }
        viewModelScope.launch {
            try {
                when (val result = getMergedHistoryUseCase(doctorId, patientId, size = HISTORY_PAGE_SIZE)) {
                    is Resource.Success -> {
                        if (generation != historyGeneration) return@launch
                        val page = result.data
                        // Backend returns newest-first (for cursor paging); render ascending, oldest at top.
                        messages.addAll(page?.items.orEmpty().asReversed())
                        nextHistoryCursor = page?.nextCursor
                        hasMoreHistory = page?.nextCursor != null
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
                            val page = result.data
                            val existingIds = messages.mapTo(HashSet()) { it.id }
                            messages.addAll(0, page?.items.orEmpty().asReversed().filterNot { it.id in existingIds })
                            nextHistoryCursor = page?.nextCursor
                            hasMoreHistory = page?.nextCursor != null
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

    /** Connects the permanent chat thread with [otherUserId] (the doctor) — no "start" step needed. */
    fun connectToThread(otherUserId: String) {
        if (currentOtherUserId == otherUserId && wsJob?.isActive == true) return
        wsJob?.cancel()
        currentOtherUserId = otherUserId
        isConnected = false
        wsJob = viewModelScope.launch(Dispatchers.IO) {
            val wsBase = Constants.BASE_URL
                .replace("https://", "wss://")
                .replace("http://", "ws://")
            var attempt = 0
            while (isActive) {
                try {
                    httpClient.webSocket("${wsBase}chat/$otherUserId") {
                        wsSession = this
                        withContext(Dispatchers.Main) { isConnected = true }
                        attempt = 0

                        // Liveness is handled by the server's own WebSocket ping/pong (see
                        // configureSockets() — pingPeriod/timeout) plus the underlying engine's
                        // automatic pong replies. A manual Frame.Ping here isn't reliably supported
                        // across Ktor's OkHttp/Darwin client engines and was closing this socket
                        // every ~25s when send() failed on it, producing a constant reconnect loop.

                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val raw = frame.readText()
                                try {
                                    when (wsJson.decodeFromString<ConsultationWsEventPeek>(raw).type) {
                                        "TYPING" -> {
                                            val event = wsJson.decodeFromString<ConsultationTypingEventData>(raw)
                                            withContext(Dispatchers.Main) { handleTypingEvent(event.isTyping) }
                                        }
                                        "PRESENCE" -> {
                                            val event = wsJson.decodeFromString<ConsultationPresenceEventData>(raw)
                                            withContext(Dispatchers.Main) {
                                                otherPartyOnline = event.isOnline
                                                otherPartyLastSeenAt = event.lastSeenAt
                                            }
                                        }
                                        // Low-latency ringing path — arrives here instantly whenever the other
                                        // party has this thread open; push (see NotificationSetup.onPayloadData)
                                        // is the fallback for backgrounded/killed apps.
                                        "CALL_INVITE" -> {
                                            val event = wsJson.decodeFromString<ConsultationCallInviteEventData>(raw)
                                            withContext(Dispatchers.Main) {
                                                IncomingCallHandler.onCallInvite(
                                                    callId = event.callId,
                                                    callerId = event.callerId,
                                                    callerName = event.callerName,
                                                    doctorId = otherUserId,
                                                    patientId = currentUserId,
                                                    isVideo = event.isVideo,
                                                    ringTimeoutSeconds = event.ringTimeoutSeconds,
                                                )
                                            }
                                        }
                                        "CALL_ANSWERED" -> {
                                            val event = wsJson.decodeFromString<ConsultationCallAnsweredEventData>(raw)
                                            withContext(Dispatchers.Main) { IncomingCallHandler.onCallAnswered(event.callId) }
                                        }
                                        "CALL_DECLINED" -> {
                                            val event = wsJson.decodeFromString<ConsultationCallDeclinedEventData>(raw)
                                            withContext(Dispatchers.Main) { IncomingCallHandler.onCallDeclined(event.callId) }
                                        }
                                        "CALL_CANCELLED" -> {
                                            val event = wsJson.decodeFromString<ConsultationCallCancelledEventData>(raw)
                                            withContext(Dispatchers.Main) { IncomingCallHandler.onCallCancelled(event.callId) }
                                        }
                                        else -> {
                                            val msg = wsJson.decodeFromString<ConsultationMessageData>(raw).toDomain()
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
                                        }
                                    }
                                } catch (e: Exception) {
                                    Napier.w(tag = "ChatTyping", message = "Failed to decode frame: ${e.message}")
                                }
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

    // Auto-clears after a few seconds in case the counterpart's client never sends the
    // "stopped typing" (isTyping=false) event — e.g. they background the app mid-type.
    private fun handleTypingEvent(isTyping: Boolean) {
        typingClearJob?.cancel()
        otherPartyTyping = isTyping
        if (isTyping) {
            typingClearJob = viewModelScope.launch {
                delay(6_000L)
                otherPartyTyping = false
            }
        }
    }

    /** Debounced — only sends isTyping=true once per burst of typing; isTyping=false always sends immediately. */
    fun sendTypingEvent(isTyping: Boolean) {
        val session = wsSession
        if (session == null) {
            Napier.w(tag = "ChatTyping", message = "sendTypingEvent(isTyping=$isTyping) dropped — no open wsSession (isConnected=$isConnected)")
            return
        }
        if (isTyping && lastTypingSentTrue) return
        lastTypingSentTrue = isTyping
        viewModelScope.launch(Dispatchers.IO) {
            try {
                session.send(Frame.Text(wsJson.encodeToString(ConsultationWsOutgoing(type = "TYPING", isTyping = isTyping))))
            } catch (e: Exception) {
                Napier.w(tag = "ChatTyping", message = "Failed to send TYPING isTyping=$isTyping: ${e.message}")
            }
        }
    }

    fun notifyCallLocked(message: String) = snackbarController.show(message)

    fun deleteThread(doctorId: String, patientId: String) {
        viewModelScope.launch {
            when (val result = deleteConversationThreadUseCase(doctorId, patientId)) {
                is Resource.Success -> threads = threads.filterNot { it.doctorId == doctorId && it.patientId == patientId }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to delete conversation", isError = true)
                else -> {}
            }
        }
    }

    /**
     * Shows a failed attachment for a file rejected on size before it was ever read. Keeps the
     * reason in front of the patient instead of a transient snackbar.
     */
    /** Shows a failed attachment for a file we could not read at all (revoked URI, etc). */
    fun rejectUnreadableFile(fileName: String, contentType: String) {
        pendingFiles.add(
            PendingFile(
                localId = "p${kotlin.random.Random.nextInt()}",
                fileName = fileName,
                contentType = contentType,
                failed = true,
                errorText = "Couldn't read this file. Please try again.",
            ),
        )
    }

    fun rejectOversizedFile(fileName: String, contentType: String, sizeBytes: Long) {
        val localId = "p${kotlin.random.Random.nextInt()}"
        pendingFiles.add(
            PendingFile(
                localId = localId,
                fileName = fileName,
                contentType = contentType,
                failed = true,
                errorText = Constants.FILE_TOO_LARGE_MESSAGE,
                // Naming both numbers makes the rejection actionable — the patient can see how
                // far over the limit they are rather than guessing.
                detailText = "${formatFileSizeDecimal(sizeBytes)} / ${formatFileSizeDecimal(Constants.MAX_CHAT_FILE_BYTES)} max",
            ),
        )
        // Clears itself so a rejection doesn't sit in the thread forever; the X dismisses it sooner.
        viewModelScope.launch {
            delay(FAILED_ATTACHMENT_AUTO_DISMISS_MS)
            dismissPendingFile(localId)
        }
    }

    /** Removes a failed attachment from the thread — the bubble's X, or the auto-dismiss timer. */
    fun dismissPendingFile(localId: String) {
        pendingFiles.removeAll { it.localId == localId }
    }

    /**
     * Queues an attachment. [openSource] is a factory rather than bytes so the upload can stream
     * the file straight to storage without ever holding it in memory.
     */
    fun sendFile(
        fileName: String,
        contentType: String,
        sizeBytes: Long,
        previewBytes: ByteArray?,
        openSource: () -> RawSource,
    ) {
        val otherUserId = currentOtherUserId
        if (otherUserId == null) {
            snackbarController.show("Not connected", isError = true)
            return
        }
        val pending = PendingFile(
            localId = "p${kotlin.random.Random.nextInt()}",
            fileName = fileName,
            contentType = contentType,
            previewBytes = previewBytes,
            totalBytes = sizeBytes,
        )
        pendingFiles.add(pending)

        // Backstop for callers that didn't check the size first.
        if (sizeBytes > Constants.MAX_CHAT_FILE_BYTES) {
            markFailed(pending, Constants.FILE_TOO_LARGE_MESSAGE)
            return
        }

        viewModelScope.launch {
            val result = consultationRepository.uploadFile(
                otherUserId = otherUserId,
                fileName = fileName,
                contentType = contentType,
                sizeBytes = sizeBytes,
                openSource = openSource,
                onProgress = { sent, total -> updateProgress(pending.localId, sent, total) },
            )
            when (result) {
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

    fun joinCall(otherUserId: String) {
        if (callJoinState is Resource.Success) return
        viewModelScope.launch {
            callJoinState = Resource.Loading()
            callJoinState = joinCallUseCase(otherUserId)
        }
    }

    fun clearCallState() {
        callJoinState = null
    }

    /** Call once the in-app Call screen tears down, so CallKit's own call state (status bar
     * indicator, Recents, Dynamic Island) doesn't linger after our UI has already moved on. */
    fun endCall() {
        ActiveCallNotifier.notifyCallEnded()
        clearCallState()
    }

    /** Rings [otherUserId] (WhatsApp-style) — does not join the meeting; see OutgoingCallState. */
    fun startCall(otherUserId: String, isVideo: Boolean, calleeName: String?) {
        viewModelScope.launch {
            when (val result = inviteToCallUseCase(otherUserId, isVideo)) {
                is Resource.Success -> {
                    val invite = result.data ?: return@launch
                    OutgoingCallState.calling(invite.callId, otherUserId, calleeName, isVideo)
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to start call", isError = true)
                else -> {}
            }
        }
    }

    fun cancelOutgoingCall(otherUserId: String, callId: String) {
        OutgoingCallState.clear()
        viewModelScope.launch { cancelCallUseCase(otherUserId, callId) }
    }

    private fun updateProgress(localId: String, sent: Long, total: Long) {
        val idx = pendingFiles.indexOfFirst { it.localId == localId }
        if (idx >= 0) pendingFiles[idx] = pendingFiles[idx].copy(sentBytes = sent, totalBytes = total)
    }

    private fun markFailed(pending: PendingFile, errorText: String? = null) {
        val idx = pendingFiles.indexOfFirst { it.localId == pending.localId }
        if (idx >= 0) pendingFiles[idx] = pending.copy(failed = true, errorText = errorText)
    }

    fun disconnect() {
        wsJob?.cancel()
        typingClearJob?.cancel()
        wsSession = null
        isConnected = false
        currentOtherUserId = null
        messages.clear()
        pendingFiles.clear()
        nextHistoryCursor = null
        hasMoreHistory = false
        otherPartyTyping = false
        otherPartyOnline = false
        otherPartyLastSeenAt = null
        lastTypingSentTrue = false
        loadThreads() // refresh list previews now that this thread may have new messages
    }

    override fun onCleared() {
        super.onCleared()
        wsJob?.cancel()
        threadsPollJob?.cancel()
    }
}
