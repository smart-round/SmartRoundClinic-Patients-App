package ke.co.smartroundclinic.patient.domain.model

/**
 * Chat threads are permanent per doctor-patient pair and can span many appointments over time, so
 * the video-call icon can't be gated off a locally cached appointment list — this is sourced from
 * GET scheduling/appointments/next (the single source of truth for "the relevant one").
 */
data class NextAppointment(
    val date: String,
    val slotStart: String,
    val slotEnd: String,
    val status: String,
)
