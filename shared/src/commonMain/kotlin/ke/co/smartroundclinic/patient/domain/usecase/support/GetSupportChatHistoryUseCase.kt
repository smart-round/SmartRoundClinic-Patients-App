package ke.co.smartroundclinic.patient.domain.usecase.support

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.SupportChatMessage
import ke.co.smartroundclinic.patient.domain.repository.SupportRepository

class GetSupportChatHistoryUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(ticketId: String): Resource<List<SupportChatMessage>> =
        repository.getChatHistory(ticketId)
}
