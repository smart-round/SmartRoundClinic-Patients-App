package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.MedicalHistoryResponse
import ke.co.smartroundclinic.patient.data.remote.dto.response.MedicalRecordResponse

interface MedicalRecordRepository {
    suspend fun getByAppointmentId(appointmentId: String): Resource<MedicalRecordResponse>
    suspend fun getMyHistory(): Resource<MedicalHistoryResponse>
}
