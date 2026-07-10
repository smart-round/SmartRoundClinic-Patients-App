package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.CallJoinInfo
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConsultationSession
import ke.co.smartroundclinic.patient.domain.model.ConversationThread

interface ConsultationRepository {
    suspend fun startOrGet(appointmentId: String): Resource<ConsultationSession>
    suspend fun getMessages(sessionId: String, page: Int, size: Int): Resource<List<ConsultationMessage>>
    suspend fun uploadFile(sessionId: String, fileName: String, contentType: String, bytes: ByteArray): Resource<ConsultationMessage>
    suspend fun joinCall(sessionId: String): Resource<CallJoinInfo>

    /** One entry per doctor-patient pair the caller participates in — merges all of their consultations. */
    suspend fun listThreads(): Resource<List<ConversationThread>>

    /** Merged, cursor-paginated history across every consultation a (doctorId, patientId) pair has had. Second value is the next page's cursor, null if exhausted. */
    suspend fun getMergedMessages(doctorId: String, patientId: String, before: String?, size: Int): Resource<Pair<List<ConsultationMessage>, String?>>
}
