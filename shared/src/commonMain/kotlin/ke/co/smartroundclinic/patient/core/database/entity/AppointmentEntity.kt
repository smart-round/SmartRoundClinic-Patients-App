package ke.co.smartroundclinic.patient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.model.Refund

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
    val refundId: String? = null,
    val refundAmount: Double? = null,
    val refundCurrency: String? = null,
    val refundStatus: String? = null,
    val refundReason: String? = null,
    val refundCreatedAt: String? = null,
    val refundUpdatedAt: String? = null,
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
    refund = if (refundId != null && refundAmount != null && refundCurrency != null && refundStatus != null && refundCreatedAt != null) {
        Refund(
            id = refundId,
            amount = refundAmount,
            currency = refundCurrency,
            status = refundStatus,
            reason = refundReason,
            createdAt = refundCreatedAt,
            updatedAt = refundUpdatedAt,
        )
    } else {
        null
    },
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
    refundId = refund?.id,
    refundAmount = refund?.amount,
    refundCurrency = refund?.currency,
    refundStatus = refund?.status,
    refundReason = refund?.reason,
    refundCreatedAt = refund?.createdAt,
    refundUpdatedAt = refund?.updatedAt,
)
