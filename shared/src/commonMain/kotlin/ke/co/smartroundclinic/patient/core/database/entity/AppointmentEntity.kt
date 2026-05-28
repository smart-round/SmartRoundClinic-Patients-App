package ke.co.smartroundclinic.patient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ke.co.smartroundclinic.patient.domain.model.Appointment

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val doctorId: String,
    val patientId: String,
    val doctorName: String?,
    val doctorProfilePicture: String?,
    val doctorSpeciality: String?,
    val date: String,
    val slotStart: String,
    val slotEnd: String,
    val status: String,
    val bookedAt: String,
    val notes: String?,
    val cancellationReason: String?,
    val cancelledBy: String?,
    val updatedAt: String?,
)

fun AppointmentEntity.toDomain() = Appointment(
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

fun Appointment.toEntity() = AppointmentEntity(
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
