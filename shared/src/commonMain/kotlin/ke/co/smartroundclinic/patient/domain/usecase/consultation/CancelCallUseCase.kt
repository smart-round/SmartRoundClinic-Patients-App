package ke.co.smartroundclinic.patient.domain.usecase.consultation

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository

class CancelCallUseCase(private val repository: ConsultationRepository) {
    suspend operator fun invoke(otherUserId: String, callId: String): Resource<Unit> =
        repository.cancelCall(otherUserId, callId)
}
