package ke.co.smartroundclinic.patient.presentation.main.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.core.notification.OutgoingCallState
import ke.co.smartroundclinic.patient.core.notification.OutgoingCallStatus
import ke.co.smartroundclinic.patient.presentation.main.chat.destinations.ConsultationCall
import ke.co.smartroundclinic.patient.presentation.main.chat.destinations.ConsultationChat
import ke.co.smartroundclinic.patient.presentation.main.chat.destinations.ConsultationList
import ke.co.smartroundclinic.patient.presentation.main.chat.destinations.OutgoingCall
import ke.co.smartroundclinic.patient.presentation.main.chat.ui.CallScreen
import ke.co.smartroundclinic.patient.presentation.main.chat.ui.ConsultationListScreen
import ke.co.smartroundclinic.patient.presentation.main.chat.ui.ConsultationScreen
import ke.co.smartroundclinic.patient.presentation.main.chat.ui.OutgoingCallScreen
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatRoot(
    modifier: Modifier = Modifier,
    onAtRootChanged: (Boolean) -> Unit = {},
    pendingConversation: ConsultationChat? = null,
    onPendingNavigated: () -> Unit = {},
    pendingCall: ConsultationCall? = null,
    onPendingCallNavigated: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    val backStack = retain { mutableStateListOf<NavKey>(ConsultationList) }
    val isAtRoot = backStack.size == 1
    val vm: ConsultationViewModel = koinViewModel()

    SideEffect { onAtRootChanged(isAtRoot) }

    // The chat list has no socket of its own — keep its online/last-seen previews live by polling
    // only while it's the visible screen (stop once a specific conversation is opened).
    LaunchedEffect(isAtRoot) {
        if (isAtRoot) vm.startThreadsPolling() else vm.stopThreadsPolling()
    }

    LaunchedEffect(pendingConversation) {
        if (pendingConversation != null) {
            backStack.removeAll { it is ConsultationChat }
            backStack.add(pendingConversation)
            onPendingNavigated()
        }
    }

    // Fires after the pendingConversation effect above (both are set together from the same
    // notification event), so ConsultationChat is already on the stack for ConsultationCall's
    // entry to read the doctor name/picture off of.
    LaunchedEffect(pendingCall) {
        if (pendingCall != null) {
            backStack.removeAll { it is ConsultationCall }
            backStack.add(pendingCall)
            onPendingCallNavigated()
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<ConsultationList> {
                ConsultationListScreen(
                    threads = vm.threads,
                    isLoading = vm.isLoadingThreads,
                    onThreadClick = { thread ->
                        backStack.add(ConsultationChat(thread.doctorId, thread.counterpartName, thread.latestAppointmentId))
                    },
                    onRefresh = vm::loadThreads,
                    onDeleteThread = { thread -> vm.deleteThread(thread.doctorId, thread.patientId) },
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick,
                )
            }
            entry<ConsultationChat> { dest ->
                LaunchedEffect(dest.doctorId) {
                    vm.connectToThread(dest.doctorId)
                    vm.loadNextAppointment(dest.doctorId)
                }
                // currentUserId loads asynchronously from Room — re-key on it so that if this fires
                // before it's populated (cold start / fresh login), it retries once the id is ready
                // instead of calling loadMergedHistory with an empty patientId forever.
                LaunchedEffect(dest.doctorId, vm.currentUserId) {
                    if (vm.currentUserId.isNotBlank()) {
                        vm.loadMergedHistory(dest.doctorId, vm.currentUserId)
                    }
                }
                val appointment = vm.appointments.firstOrNull { it.id == dest.latestAppointmentId }
                val doctorPicture = vm.threads.firstOrNull { it.doctorId == dest.doctorId }?.counterpartPicture
                    ?: appointment?.let { vm.doctorPicture(it.doctorId) }
                ConsultationScreen(
                    doctorName = dest.doctorName,
                    doctorPicture = doctorPicture,
                    appointment = vm.nextAppointment,
                    onLockedCallClick = { vm.notifyCallLocked(it) },
                    messages = vm.messages,
                    isLoadingHistory = vm.isLoadingHistory,
                    isLoadingMoreHistory = vm.isLoadingMoreHistory,
                    hasMoreHistory = vm.hasMoreHistory,
                    onLoadMoreHistory = { vm.loadMoreHistory(dest.doctorId, vm.currentUserId) },
                    isConnected = vm.isConnected,
                    isUploadingFile = vm.isUploadingFile,
                    pendingFiles = vm.pendingFiles,
                    currentUserId = vm.currentUserId,
                    otherPartyTyping = vm.otherPartyTyping,
                    otherPartyOnline = vm.otherPartyOnline,
                    otherPartyLastSeenAt = vm.otherPartyLastSeenAt,
                    onTyping = vm::sendTypingEvent,
                    onBack = {
                        vm.disconnect()
                        backStack.removeLastOrNull()
                    },
                    onVideoCall = { backStack.add(OutgoingCall(dest.doctorId, dest.doctorName, isVideo = true)) },
                    onSendText = vm::sendText,
                    onSendFile = vm::sendFile,
                    onFileTooLarge = vm::rejectOversizedFile,
                    onSendFileFailed = vm::rejectUnreadableFile,
                )
            }
            entry<OutgoingCall> { dest ->
                LaunchedEffect(dest.otherUserId) {
                    vm.startCall(dest.otherUserId, dest.isVideo, dest.calleeName)
                }
                DisposableEffect(dest.otherUserId) {
                    onDispose {
                        val status = OutgoingCallState.current.value
                        if (status is OutgoingCallStatus.Calling && status.otherUserId == dest.otherUserId) {
                            vm.cancelOutgoingCall(dest.otherUserId, status.callId)
                        }
                    }
                }

                val status by OutgoingCallState.current.collectAsState()
                LaunchedEffect(status) {
                    when (val s = status) {
                        is OutgoingCallStatus.Answered -> {
                            OutgoingCallState.clear()
                            backStack.removeLastOrNull()
                            backStack.add(ConsultationCall(dest.otherUserId, isVideo = dest.isVideo))
                        }
                        is OutgoingCallStatus.Declined -> {
                            OutgoingCallState.clear()
                            backStack.removeLastOrNull()
                        }
                        is OutgoingCallStatus.Calling -> {
                            // Ring timeout mirrors the backend's RedisKeys.CALL_INVITE_TTL_SECONDS —
                            // if nobody answers in time, cancel on the caller's own initiative too.
                            delay(45_000L)
                            if (OutgoingCallState.current.value == s) {
                                vm.cancelOutgoingCall(dest.otherUserId, s.callId)
                                backStack.removeLastOrNull()
                            }
                        }
                        null -> Unit
                    }
                }

                OutgoingCallScreen(
                    calleeName = dest.calleeName,
                    statusText = when (status) {
                        is OutgoingCallStatus.Declined -> "Call declined"
                        else -> "Calling…"
                    },
                    onCancel = {
                        val s = OutgoingCallState.current.value
                        if (s is OutgoingCallStatus.Calling) vm.cancelOutgoingCall(dest.otherUserId, s.callId)
                        else OutgoingCallState.clear()
                        backStack.removeLastOrNull()
                    },
                )
            }
            entry<ConsultationCall> { dest ->
                val callChat = backStack.filterIsInstance<ConsultationChat>().firstOrNull()
                val doctorName = callChat?.doctorName ?: "Doctor"
                val doctorPicture = callChat?.let { c ->
                    vm.threads.firstOrNull { it.doctorId == c.doctorId }?.counterpartPicture
                        ?: vm.appointments.firstOrNull { it.id == c.latestAppointmentId }?.let { vm.doctorPicture(it.doctorId) }
                }
                CallScreen(
                    doctorName = doctorName,
                    doctorPicture = doctorPicture,
                    selfPicture = vm.currentUserProfilePicture,
                    isVideo = dest.isVideo,
                    joinState = vm.callJoinState,
                    onJoin = { vm.joinCall(dest.otherUserId) },
                    onEnd = {
                        vm.endCall()
                        backStack.removeLastOrNull()
                    },
                )
            }
        },
    )
}
