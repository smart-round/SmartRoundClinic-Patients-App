package ke.co.smartroundclinic.patient.domain.usecase.consultation

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.CallInvite
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository

class InviteToCallUseCase(private val repository: ConsultationRepository) {
    suspend operator fun invoke(otherUserId: String, isVideo: Boolean = true): Resource<CallInvite> =
        repository.inviteToCall(otherUserId, isVideo)
}
