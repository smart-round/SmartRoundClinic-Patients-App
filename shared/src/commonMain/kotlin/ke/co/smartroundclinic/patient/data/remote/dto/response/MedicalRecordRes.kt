package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.MedicalRecord
import ke.co.smartroundclinic.patient.domain.model.PrescriptionItem
import kotlinx.serialization.Serializable

@Serializable
data class PrescriptionItemData(
    val drug: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val instructions: String? = null,
)

@Serializable
data class MedicalRecordData(
    val id: String,
    val appointmentId: String,
    val consultationId: String? = null,
    val doctorId: String,
    val patientId: String,
    val diagnosis: String? = null,
    val prescription: List<PrescriptionItemData> = emptyList(),
    val summary: String? = null,
    val referralNote: String? = null,
    val labRequests: List<String> = emptyList(),
    val additionalNotes: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
)

@Serializable
data class MedicalRecordResponse(
    val data: MedicalRecordData? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class MedicalHistoryResponse(
    val data: List<MedicalRecordData>? = null,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

fun PrescriptionItemData.toDomain() = PrescriptionItem(drug, dosage, frequency, duration, instructions)

fun MedicalRecordData.toDomain() = MedicalRecord(
    id = id,
    appointmentId = appointmentId,
    consultationId = consultationId,
    doctorId = doctorId,
    patientId = patientId,
    diagnosis = diagnosis,
    prescription = prescription.map { it.toDomain() },
    summary = summary,
    referralNote = referralNote,
    labRequests = labRequests,
    additionalNotes = additionalNotes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
