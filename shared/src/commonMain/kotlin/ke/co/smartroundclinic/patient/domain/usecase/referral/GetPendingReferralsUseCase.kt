package ke.co.smartroundclinic.patient.domain.usecase.referral

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Referral
import ke.co.smartroundclinic.patient.domain.repository.ReferralRepository

class GetPendingReferralsUseCase(private val repository: ReferralRepository) {
    suspend operator fun invoke(): Resource<List<Referral>> {
        val result = repository.getPendingReferrals()
        val response = result.data ?: return Resource.Error(result.message ?: "Failed to load referrals")
        return Resource.Success(response.data.orEmpty().map { it.toDomain() })
    }
}
