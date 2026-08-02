package ke.co.smartroundclinic.patient.core.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class NotificationEvent {
    data object ToNotifications : NotificationEvent()
    data object ToMedicalHistory : NotificationEvent()
    data class ToAppointmentDetail(val appointmentId: String) : NotificationEvent()
    data class ToConsultationChat(val doctorId: String, val doctorName: String, val appointmentId: String) : NotificationEvent()
    data class ToCall(val doctorId: String, val doctorName: String, val appointmentId: String) : NotificationEvent()
    data class ToSupportTicket(val ticketId: String) : NotificationEvent()
    data object ToReferrals : NotificationEvent()
}

object NotificationDeepLink {
    private val _pendingEvent = MutableStateFlow<NotificationEvent?>(null)
    val pendingEvent = _pendingEvent.asStateFlow()

    fun signal(event: NotificationEvent) { _pendingEvent.value = event }
    fun consume() { _pendingEvent.value = null }
}
