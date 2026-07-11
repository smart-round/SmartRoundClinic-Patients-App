package ke.co.smartroundclinic.patient.core.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

private val APPOINTMENT_TIMEZONE = TimeZone.of("Africa/Nairobi")

/** Parses an appointment's `date` ("yyyy-MM-dd") + `slotStart`/`slotEnd` ("HH:mm") into an [Instant], anchored to the clinic's timezone — mirrors the backend's parsing in CompleteAppointmentUseCase. */
fun parseAppointmentInstant(date: String, time: String): Instant? = runCatching {
    val (h, m) = time.split(":").map { it.toInt() }
    val dateParts = date.split("-").map { it.toInt() }
    LocalDateTime(dateParts[0], dateParts[1], dateParts[2], h, m, 0, 0)
        .toInstant(APPOINTMENT_TIMEZONE)
}.getOrNull()
