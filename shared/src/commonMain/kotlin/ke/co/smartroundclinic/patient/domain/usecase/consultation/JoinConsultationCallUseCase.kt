package ke.co.smartroundclinic.patient.domain.usecase.consultation

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.CallJoinInfo
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository

class JoinConsultationCallUseCase(private val repository: ConsultationRepository) {
    suspend operator fun invoke(sessionId: String): Resource<CallJoinInfo> =
        repository.joinCall(sessionId)
}
