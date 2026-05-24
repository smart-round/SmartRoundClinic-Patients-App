package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConsultationSession

interface ConsultationRepository {
    suspend fun startOrGet(appointmentId: String): Resource<ConsultationSession>
    suspend fun getMessages(sessionId: String, page: Int, size: Int): Resource<List<ConsultationMessage>>
    suspend fun uploadFile(sessionId: String, fileName: String, contentType: String, bytes: ByteArray): Resource<ConsultationMessage>
}
