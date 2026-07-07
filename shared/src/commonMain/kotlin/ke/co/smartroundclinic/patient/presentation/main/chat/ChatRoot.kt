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
    onGoToAppointmentDetail: (String) -> Unit = {},
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
                    appointments = vm.appointments,
                    isLoading = vm.isLoadingAppointments,
                    onAppointmentClick = { appointment ->
                        val isCompleted = appointment.status.equals("COMPLETED", ignoreCase = true)
                        if (isCompleted || canJoinConsultation(appointment)) {
                            backStack.add(ConsultationChat(appointment.id, vm.doctorName(appointment.doctorId)))
                        }
                    },
                    onRefresh = vm::loadAppointments,
                    doctorName = vm::doctorName,
                    doctorPicture = vm::doctorPicture,
                    canJoin = ::canJoinConsultation,
                    isOverdue = ::isOverdueConfirmed,
                    onCancelClick = { appointment -> onGoToAppointmentDetail(appointment.id) },
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick,
                )
            }
            entry<ConsultationChat> { dest ->
                LaunchedEffect(dest.appointmentId) {
                    vm.startConsultation(dest.appointmentId)
                }
                val appointment = vm.appointments.firstOrNull { it.id == dest.appointmentId }
                val isCompleted = appointment?.status?.equals("COMPLETED", ignoreCase = true) == true
                val canJoinCall = appointment?.let { appt ->
                    try {
                        val tz = TimeZone.currentSystemDefault()
                        val date = LocalDate.parse(appt.date)
                        val parts = appt.slotStart.split(":")
                        val hour = parts[0].toInt()
                        val minute = parts.getOrNull(1)?.toInt() ?: 0
                        val slotInstant = LocalDateTime(date, LocalTime(hour, minute)).toInstant(tz)
                        val diffMinutes = (Clock.System.now() - slotInstant).inWholeMinutes
                        diffMinutes in -5..60
                    } catch (_: Exception) { false }
                } ?: false
                ConsultationScreen(
                    doctorName = dest.doctorName,
                    doctorPicture = appointment?.let { vm.doctorPicture(it.doctorId) },
                    session = vm.activeSession,
                    messages = vm.messages,
                    isStartingSession = vm.isStartingSession,
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
                    vm.appointments.firstOrNull { it.id == c.appointmentId }?.let { vm.doctorPicture(it.doctorId) }
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

internal fun isOverdueConfirmed(appointment: Appointment): Boolean {
    if (!appointment.status.equals("CONFIRMED", ignoreCase = true)) return false
    return try {
        val tz = TimeZone.currentSystemDefault()
        val date = LocalDate.parse(appointment.date)
        val parts = appointment.slotStart.split(":")
        val hour = parts[0].toInt()
        val minute = parts.getOrNull(1)?.toInt() ?: 0
        val slotInstant = LocalDateTime(date, LocalTime(hour, minute)).toInstant(tz)
        val diffMinutes = (Clock.System.now() - slotInstant).inWholeMinutes
        diffMinutes > 60
    } catch (_: Exception) {
        false
    }
}
