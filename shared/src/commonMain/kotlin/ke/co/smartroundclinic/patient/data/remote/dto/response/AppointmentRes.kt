package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.model.NextAppointment
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
    val doctorName: String? = null,
    val doctorProfilePicture: String? = null,
    val doctorSpeciality: String? = null,
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

@Serializable
data class NextAppointmentRes(
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
    val data: NextAppointmentData? = null,
)

@Serializable
data class NextAppointmentData(
    val id: String,
    val doctorId: String,
    val patientId: String,
    val date: String,
    val slotStart: String,
    val slotEnd: String,
    val status: String,
)

fun NextAppointmentData.toDomain() = NextAppointment(
    date = date,
    slotStart = slotStart,
    slotEnd = slotEnd,
    status = status,
)

fun AppointmentData.toDomain() = Appointment(
    id = id,
    doctorId = doctorId,
    patientId = patientId,
    doctorName = doctorName,
    doctorProfilePicture = doctorProfilePicture,
    doctorSpeciality = doctorSpeciality,
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
