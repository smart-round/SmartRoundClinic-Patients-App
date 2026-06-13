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
) {
    val backStack = retain { mutableStateListOf<NavKey>(ConsultationList) }
    val isAtRoot = backStack.size == 1
    val vm: ConsultationViewModel = koinViewModel()

    SideEffect { onAtRootChanged(isAtRoot) }

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
                        backStack.add(ConsultationChat(appointment.id, vm.doctorName(appointment.doctorId)))
                    },
                    onRefresh = vm::loadAppointments,
                    doctorName = vm::doctorName,
                    doctorPicture = vm::doctorPicture,
                    canJoin = ::canJoinConsultation,
                )
            }
            entry<ConsultationChat> { dest ->
                LaunchedEffect(dest.appointmentId) {
                    vm.startConsultation(dest.appointmentId)
                }
                val appointment = vm.appointments.firstOrNull { it.id == dest.appointmentId }
                val isCompleted = appointment?.status?.equals("COMPLETED", ignoreCase = true) == true
                val canJoinCall = appointment?.let { canJoinConsultation(it) } ?: false
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
                val doctorName = backStack
                    .filterIsInstance<ConsultationChat>()
                    .firstOrNull()?.doctorName ?: "Doctor"
                CallScreen(
                    doctorName = doctorName,
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
        diffMinutes in -30..30
    } catch (_: Exception) {
        false
    }
}
