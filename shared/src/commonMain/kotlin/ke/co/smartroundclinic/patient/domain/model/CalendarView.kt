package ke.co.smartroundclinic.patient.domain.model

data class CalendarView(
    val doctorId: String,
    val days: List<CalendarDay>,
)

data class CalendarDay(
    val date: String,
    val dayOfWeek: String,
    val isWorkingDay: Boolean,
    val slots: List<CalendarSlot>,
)

data class CalendarSlot(
    val slotStart: String,
    val slotEnd: String,
    val status: String,
    val appointmentId: String?,
    val patientId: String?,
)
