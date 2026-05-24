package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.CalendarDay
import ke.co.smartroundclinic.patient.domain.model.CalendarSlot
import ke.co.smartroundclinic.patient.domain.model.CalendarView
import kotlinx.serialization.Serializable

@Serializable
data class CalendarRes(
    val data: CalendarResData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class CalendarResData(
    val doctorId: String,
    val days: List<CalendarDayData>,
)

@Serializable
data class CalendarDayData(
    val date: String,
    val dayOfWeek: String,
    val isWorkingDay: Boolean,
    val slots: List<CalendarSlotData>,
)

@Serializable
data class CalendarSlotData(
    val slotStart: String,
    val slotEnd: String,
    val status: String,
    val appointmentId: String? = null,
    val patientId: String? = null,
)

fun CalendarResData.toDomain() = CalendarView(
    doctorId = doctorId,
    days = days.map { it.toDomain() },
)

fun CalendarDayData.toDomain() = CalendarDay(
    date = date,
    dayOfWeek = dayOfWeek,
    isWorkingDay = isWorkingDay,
    slots = slots.map { it.toDomain() },
)

fun CalendarSlotData.toDomain() = CalendarSlot(
    slotStart = slotStart,
    slotEnd = slotEnd,
    status = status,
    appointmentId = appointmentId,
    patientId = patientId,
)
