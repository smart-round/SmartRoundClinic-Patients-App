package ke.co.smartroundclinic.patient.domain.usecase.referral

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Referral
import ke.co.smartroundclinic.patient.domain.repository.ReferralRepository

class DeclineReferralUseCase(private val repository: ReferralRepository) {
    suspend operator fun invoke(referralId: String): Resource<Referral?> {
        val result = repository.declineReferral(referralId)
        val response = result.data ?: return Resource.Error(result.message ?: "Failed to decline referral")
        return Resource.Success(response.data?.toDomain())
    }
}
