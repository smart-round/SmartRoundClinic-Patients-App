package ke.co.smartroundclinic.patient.presentation.main.chat.util

import ke.co.smartroundclinic.patient.core.util.parseAppointmentInstant
import ke.co.smartroundclinic.patient.domain.model.NextAppointment
import kotlinx.datetime.Instant
import kotlinx.datetime.minus
import kotlin.time.Duration.Companion.minutes

private val JOIN_WINDOW_LEAD = 10.minutes

sealed class CallAvailability {
    data object Hidden : CallAvailability()
    data class Locked(val availableAt: Instant) : CallAvailability()
    data object Available : CallAvailability()
}

/** The video-call icon is only ever shown for a CONFIRMED appointment, and only becomes joinable
 * from 10 minutes before its slotStart onward (no upper bound — it stays joinable for as long as
 * the appointment remains CONFIRMED, mirroring the backend's hasJoinableConfirmedAppointment check). */
fun callAvailability(appointment: NextAppointment?, now: Instant): CallAvailability {
    if (appointment == null || appointment.status != "CONFIRMED") return CallAvailability.Hidden
    val slotStartInstant = parseAppointmentInstant(appointment.date, appointment.slotStart) ?: return CallAvailability.Hidden
    val unlockInstant = slotStartInstant - JOIN_WINDOW_LEAD
    return if (now >= unlockInstant) CallAvailability.Available else CallAvailability.Locked(slotStartInstant)
}
