package ke.co.smartroundclinic.patient.domain.usecase.consultation

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.ConsultationMessage
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository

class GetConsultationMessagesUseCase(private val repository: ConsultationRepository) {
    suspend operator fun invoke(
        sessionId: String,
        page: Int = 1,
        size: Int = 50,
    ): Resource<List<ConsultationMessage>> = repository.getMessages(sessionId, page, size)
}
