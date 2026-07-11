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
 * from 10 minutes before its slotStart onward. There's no local upper-bound check here — once an
 * appointment is more than 24h past its slotStart, the backend's GET /scheduling/appointments/next
 * simply stops returning it (mirroring hasJoinableConfirmedAppointment's own 24h expiry), so
 * [appointment] arrives as null and this naturally falls through to [CallAvailability.Hidden]. */
fun callAvailability(appointment: NextAppointment?, now: Instant): CallAvailability {
    if (appointment == null || appointment.status != "CONFIRMED") return CallAvailability.Hidden
    val slotStartInstant = parseAppointmentInstant(appointment.date, appointment.slotStart) ?: return CallAvailability.Hidden
    val unlockInstant = slotStartInstant - JOIN_WINDOW_LEAD
    return if (now >= unlockInstant) CallAvailability.Available else CallAvailability.Locked(slotStartInstant)
}
