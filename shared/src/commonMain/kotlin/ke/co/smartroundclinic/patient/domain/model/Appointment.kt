package ke.co.smartroundclinic.patient.domain.model

data class Appointment(
    val id: String,
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
    val refund: Refund? = null,
)
