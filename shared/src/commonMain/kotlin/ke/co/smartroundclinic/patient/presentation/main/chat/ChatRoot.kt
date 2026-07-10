package ke.co.smartroundclinic.patient.presentation.main.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.presentation.main.chat.destinations.ConsultationCall
import ke.co.smartroundclinic.patient.presentation.main.chat.destinations.ConsultationChat
import ke.co.smartroundclinic.patient.presentation.main.chat.destinations.ConsultationList
import ke.co.smartroundclinic.patient.presentation.main.chat.ui.CallScreen
import ke.co.smartroundclinic.patient.presentation.main.chat.ui.ConsultationListScreen
import ke.co.smartroundclinic.patient.presentation.main.chat.ui.ConsultationScreen

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.Clock

@Composable
fun ChatRoot(
    modifier: Modifier = Modifier,
    onAtRootChanged: (Boolean) -> Unit = {},
    pendingConversation: ConsultationChat? = null,
    onPendingNavigated: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    val backStack = retain { mutableStateListOf<NavKey>(ConsultationList) }
    val isAtRoot = backStack.size == 1
    val vm: ConsultationViewModel = koinViewModel()

    SideEffect { onAtRootChanged(isAtRoot) }

    LaunchedEffect(pendingConversation) {
        if (pendingConversation != null) {
            backStack.removeAll { it is ConsultationChat }
            backStack.add(pendingConversation)
            onPendingNavigated()
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
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick,
                )
            }
            entry<ConsultationChat> { dest ->
                LaunchedEffect(dest.latestAppointmentId) {
                    vm.startConsultation(dest.latestAppointmentId)
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
                val isCompleted = appointment?.status?.equals("COMPLETED", ignoreCase = true) == true
                val canJoinCall = appointment?.let { canJoinConsultation(it) } ?: false
                val doctorPicture = vm.threads.firstOrNull { it.doctorId == dest.doctorId }?.counterpartPicture
                    ?: appointment?.let { vm.doctorPicture(it.doctorId) }
                ConsultationScreen(
                    doctorName = dest.doctorName,
                    doctorPicture = doctorPicture,
                    session = vm.activeSession,
                    messages = vm.messages,
                    isStartingSession = vm.isStartingSession,
                    isLoadingHistory = vm.isLoadingHistory,
                    isLoadingMoreHistory = vm.isLoadingMoreHistory,
                    hasMoreHistory = vm.hasMoreHistory,
                    onLoadMoreHistory = { vm.loadMoreHistory(dest.doctorId, vm.currentUserId) },
                    isConnected = vm.isConnected,
                    isUploadingFile = vm.isUploadingFile,
                    isCompleted = isCompleted,
                    canJoinCall = canJoinCall,
                    pendingFiles = vm.pendingFiles,
                    currentUserId = vm.currentUserId,
                    onBack = {
                        vm.endConsultation()
                        backStack.removeLastOrNull()
                    },
                    onVideoCall = { backStack.add(ConsultationCall(vm.activeSession?.id ?: "", isVideo = true)) },
                    onSendText = vm::sendText,
                    onSendFile = vm::sendFile,
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
                    onJoin = { vm.joinCall(dest.sessionId) },
                    onEnd = {
                        vm.clearCallState()
                        backStack.removeLastOrNull()
                    },
                )
            }
        },
    )
}

internal fun canJoinConsultation(appointment: Appointment): Boolean {
    return try {
        val tz = TimeZone.currentSystemDefault()
        val date = LocalDate.parse(appointment.date)
        val timeParts = appointment.slotStart.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts.getOrNull(1)?.toInt() ?: 0
        val slotInstant = LocalDateTime(date, LocalTime(hour, minute)).toInstant(tz)
        val now = Clock.System.now()
        val diffMinutes = (now - slotInstant).inWholeMinutes
        diffMinutes in -5..60
    } catch (_: Exception) {
        false
    }
}
