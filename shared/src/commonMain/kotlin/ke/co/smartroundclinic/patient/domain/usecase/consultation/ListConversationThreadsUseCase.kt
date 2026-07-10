package ke.co.smartroundclinic.patient.domain.usecase.consultation

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.ConversationThread
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository

class ListConversationThreadsUseCase(private val repository: ConsultationRepository) {
    suspend operator fun invoke(): Resource<List<ConversationThread>> = repository.listThreads()
}
