package ke.co.smartroundclinic.patient.core.notification

import com.mmk.kmpnotifier.notification.NotifierManager
import io.github.aakira.napier.Napier
import ke.co.smartroundclinic.patient.domain.repository.AppointmentLocalRepository
import ke.co.smartroundclinic.patient.domain.usecase.notification.RegisterDeviceTokenUseCase
import ke.co.smartroundclinic.patient.notificationPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val TAG = "NotificationSetup"

// Push transport TTLs (see the backend's ApnsVoipClient/PushNotificationRepositoryImpl) already
// stop a stale invite from being delivered at all in the common case, but this is a second,
// independent line of defense for anything that slips through (e.g. a push held right at the TTL
// boundary) — a device just back online should never ring for a call the caller already gave up
// on. A few extra seconds of slack absorbs clock skew between this device and the backend.
private val STALE_INVITE_GRACE = 5.seconds

/** True if an invite this old (relative to its own ring window) is stale and should be dropped rather than rung. */
fun isStaleInvite(createdAt: String?, ringTimeoutSeconds: Long): Boolean {
    val createdInstant = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return false
    val age = Clock.System.now() - createdInstant
    return age > ringTimeoutSeconds.seconds + STALE_INVITE_GRACE
}

fun setupNotificationListener() {
    val scope = CoroutineScope(Dispatchers.IO)
    val component = object : KoinComponent {
        val registerDeviceToken: RegisterDeviceTokenUseCase by inject()
        val appointmentLocalRepository: AppointmentLocalRepository by inject()
    }

    NotifierManager.addListener(object : NotifierManager.Listener {
        override fun onNewToken(token: String) {
            Napier.d(tag = TAG, message = "FCM token refreshed: $token")
            scope.launch {
                val result = component.registerDeviceToken(token, notificationPlatform)
                Napier.d(tag = TAG, message = "Token registration result (onNewToken): $result")
            }
        }

        // Data-only messages (call invite/answer/decline/cancel) — fires on receipt, including
        // while backgrounded, unlike onNotificationClicked which only fires on tap.
        override fun onPayloadData(data: Map<String, *>) {
            val event = data["event"]?.toString() ?: return
            val callId = data["callId"]?.toString() ?: return
            Napier.d(tag = TAG, message = "Call signal received: event=$event callId=$callId")
            when (event) {
                "Incoming Video Call" -> {
                    val doctorId = data["doctorId"]?.toString() ?: return
                    val patientId = data["patientId"]?.toString() ?: return
                    val ringTimeoutSeconds = data["ringTimeoutSeconds"]?.toString()?.toLongOrNull() ?: 60L
                    if (isStaleInvite(data["createdAt"]?.toString(), ringTimeoutSeconds)) {
                        Napier.w(tag = TAG, message = "Dropping stale Incoming Video Call push callId=$callId — invite already past its ring window")
                        return
                    }
                    IncomingCallHandler.onCallInvite(
                        callId = callId,
                        callerId = data["callerId"]?.toString() ?: return,
                        callerName = data["callerName"]?.toString(),
                        doctorId = doctorId,
                        patientId = patientId,
                        isVideo = data["isVideo"]?.toString()?.toBooleanStrictOrNull() ?: true,
                        ringTimeoutSeconds = ringTimeoutSeconds,
                    )
                }
                "Call Answered" -> IncomingCallHandler.onCallAnswered(callId)
                "Call Declined" -> IncomingCallHandler.onCallDeclined(callId)
                "Call Cancelled" -> IncomingCallHandler.onCallCancelled(callId)
                else -> Napier.w(tag = TAG, message = "onPayloadData: unrecognized event '$event' — no call-signal handler for it")
            }
        }

        override fun onNotificationClicked(data: Map<String, Any?>) {
            val event = data["event"]?.toString()
            val appointmentId = data["appointmentId"]?.toString()
            val ticketId = data["ticketId"]?.toString()
            val doctorId = data["doctorId"]?.toString()
            val doctorName = (data["doctorName"] ?: data["senderName"])?.toString() ?: "Doctor"
            val callId = data["callId"]?.toString()

            Napier.d(tag = TAG, message = "Notification tapped — event=$event appointmentId=$appointmentId doctorId=$doctorId ticketId=$ticketId")

            // Call/chat lifecycle events (Doctor Joined the Call, Call Ended, New Chat Message for a
            // consultation thread) never carry appointmentId on the backend — they carry doctorId
            // directly. Resolving via a locally-cached appointment lookup is only a fallback for the
            // rare case doctorId itself is missing; it must never be the gate that blocks routing.
            suspend fun resolvedDoctorId(appointmentId: String?): String? = doctorId
                ?: appointmentId?.let { id -> component.appointmentLocalRepository.getAll().firstOrNull { it.id == id }?.doctorId }

            suspend fun toConsultationChat(appointmentId: String?): NotificationEvent {
                val resolved = resolvedDoctorId(appointmentId)
                return if (!resolved.isNullOrBlank()) NotificationEvent.ToConsultationChat(resolved, doctorName, appointmentId.orEmpty())
                else NotificationEvent.ToNotifications
            }

            // A join now requires a live invite's callId (see the backend's atomic-join gate) — if
            // this notification didn't carry one (or it's since expired), there's nothing this tap
            // can join into, so land on the chat thread instead of a CallScreen with no callId.
            suspend fun toCall(appointmentId: String?): NotificationEvent {
                val resolved = resolvedDoctorId(appointmentId)
                return if (!resolved.isNullOrBlank() && !callId.isNullOrBlank()) NotificationEvent.ToCall(resolved, doctorName, appointmentId.orEmpty(), callId)
                else if (!resolved.isNullOrBlank()) NotificationEvent.ToConsultationChat(resolved, doctorName, appointmentId.orEmpty())
                else NotificationEvent.ToNotifications
            }

            scope.launch {
                val notifEvent: NotificationEvent = when (event) {
                    "Appointment Booked",
                    "Appointment Confirmed",
                    "Appointment Cancelled",
                    "Appointment Completed",
                    "Missed Appointment" -> {
                        if (!appointmentId.isNullOrBlank()) NotificationEvent.ToAppointmentDetail(appointmentId)
                        else NotificationEvent.ToNotifications
                    }
                    "Doctor is Ready",
                    "Doctor Joined the Call" -> toCall(appointmentId)
                    "Consultation Ended",
                    "Call Ended" -> toConsultationChat(appointmentId)
                    "New Chat Message" -> when {
                        doctorId != null || !appointmentId.isNullOrBlank() -> toConsultationChat(appointmentId)
                        !ticketId.isNullOrBlank() -> NotificationEvent.ToSupportTicket(ticketId)
                        else -> NotificationEvent.ToNotifications
                    }
                    "Support ticket status updated" -> {
                        if (!ticketId.isNullOrBlank()) NotificationEvent.ToSupportTicket(ticketId)
                        else NotificationEvent.ToNotifications
                    }
                    "Medical Record Updated" -> NotificationEvent.ToMedicalHistory
                    "You've Been Referred" -> NotificationEvent.ToReferrals
                    null -> NotificationEvent.ToNotifications
                    else -> {
                        Napier.w(tag = TAG, message = "onNotificationClicked: unrecognized event '$event' — falling back to Notifications")
                        NotificationEvent.ToNotifications
                    }
                }

                NotificationDeepLink.signal(notifEvent)
            }
        }
    })

    scope.launch {
        val token = NotifierManager.getPushNotifier().getToken()
        Napier.d(tag = TAG, message = "Current FCM token on startup: $token")
        if (token != null) {
            val result = component.registerDeviceToken(token, notificationPlatform)
            Napier.d(tag = TAG, message = "Token registration result (startup): $result")
        } else {
            Napier.w(tag = TAG, message = "FCM token is null on startup — Firebase may not be initialized yet")
        }
    }
}
