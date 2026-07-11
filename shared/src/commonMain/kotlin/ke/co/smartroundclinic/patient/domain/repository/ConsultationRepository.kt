package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.CallInvite
import ke.co.smartroundclinic.patient.domain.model.CallJoinInfo
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConversationThread
import ke.co.smartroundclinic.patient.domain.model.MergedHistoryPage

interface ConsultationRepository {
    suspend fun uploadFile(otherUserId: String, fileName: String, contentType: String, bytes: ByteArray): Resource<ConsultationMessage>
    suspend fun joinCall(otherUserId: String): Resource<CallJoinInfo>

    /** Rings the other party (WhatsApp-style) — does not join the meeting itself, see JoinConsultationCallUseCase. */
    suspend fun inviteToCall(otherUserId: String, isVideo: Boolean): Resource<CallInvite>
    suspend fun declineCall(otherUserId: String, callId: String): Resource<Unit>
    suspend fun cancelCall(otherUserId: String, callId: String): Resource<Unit>

    /** One entry per doctor-patient pair the caller participates in — merges all of their consultations. */
    suspend fun listThreads(): Resource<List<ConversationThread>>

    /** Merged, cursor-paginated history for the permanent (doctorId, patientId) thread. */
    suspend fun getMergedMessages(doctorId: String, patientId: String, before: String?, size: Int): Resource<MergedHistoryPage>

    /** Hides the thread from the caller's own list only ("delete for me") — reappears on a new message. */
    suspend fun deleteThread(doctorId: String, patientId: String): Resource<Unit>
}
