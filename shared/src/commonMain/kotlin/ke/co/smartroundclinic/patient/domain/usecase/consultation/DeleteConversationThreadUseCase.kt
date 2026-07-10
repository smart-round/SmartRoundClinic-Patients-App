package ke.co.smartroundclinic.patient.domain.usecase.consultation

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository

class DeleteConversationThreadUseCase(private val repository: ConsultationRepository) {
    suspend operator fun invoke(doctorId: String, patientId: String): Resource<Unit> =
        repository.deleteThread(doctorId, patientId)
}
