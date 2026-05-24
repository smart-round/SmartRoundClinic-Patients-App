package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.Appointment
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentRes(
    val data: AppointmentData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class AppointmentsListRes(
    val data: List<AppointmentData> = emptyList(),
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class AppointmentData(
    val id: String,
    val doctorId: String,
    val patientId: String,
    val date: String,
    val slotStart: String,
    val slotEnd: String,
    val status: String,
    val bookedAt: String,
    val notes: String? = null,
    val cancellationReason: String? = null,
    val cancelledBy: String? = null,
    val updatedAt: String? = null,
)

fun AppointmentData.toDomain() = Appointment(
    id = id,
    doctorId = doctorId,
    patientId = patientId,
    date = date,
    slotStart = slotStart,
    slotEnd = slotEnd,
    status = status,
    bookedAt = bookedAt,
    notes = notes,
    cancellationReason = cancellationReason,
    cancelledBy = cancelledBy,
    updatedAt = updatedAt,
)
