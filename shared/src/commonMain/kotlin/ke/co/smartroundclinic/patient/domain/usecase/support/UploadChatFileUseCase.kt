package ke.co.smartroundclinic.patient.domain.usecase.support

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.SupportChatMessage
import ke.co.smartroundclinic.patient.domain.repository.SupportRepository

class UploadChatFileUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(
        ticketId: String,
        bytes: ByteArray,
        fileName: String,
        contentType: String,
    ): Resource<SupportChatMessage> = repository.uploadChatFile(ticketId, bytes, fileName, contentType)
}
