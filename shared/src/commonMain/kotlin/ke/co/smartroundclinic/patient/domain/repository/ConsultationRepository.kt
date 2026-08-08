package ke.co.smartroundclinic.patient.domain.repository

import kotlinx.io.RawSource
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.CallInvite
import ke.co.smartroundclinic.patient.domain.model.CallJoinInfo
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.model.ConversationThread
import ke.co.smartroundclinic.patient.domain.model.MergedHistoryPage

interface ConsultationRepository {
    /**
     * Uploads an attachment by streaming it straight to storage.
     *
     * Takes an [openSource] factory rather than a ByteArray so the file is never held in memory
     * — a 300MB attachment would otherwise exhaust the heap long before it reached the network.
     * The factory may be called more than once if the transfer has to be restarted.
     */
    suspend fun uploadFile(
        otherUserId: String,
        fileName: String,
        contentType: String,
        sizeBytes: Long,
        openSource: () -> RawSource,
        /** Reports bytes written so far. Called on the upload's IO context, not the main thread. */
        onProgress: (sent: Long, total: Long) -> Unit = { _, _ -> },
    ): Resource<ConsultationMessage>
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
