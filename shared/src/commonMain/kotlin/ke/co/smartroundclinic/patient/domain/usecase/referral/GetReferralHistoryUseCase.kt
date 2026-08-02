package ke.co.smartroundclinic.patient.domain.usecase.referral

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Referral
import ke.co.smartroundclinic.patient.domain.repository.ReferralRepository

/** Every referral for this patient, any status — unlike [GetPendingReferralsUseCase], this also
 * surfaces already accepted/declined ones so a patient who changes their mind can still find and
 * book with a previously-referred doctor. */
class GetReferralHistoryUseCase(private val repository: ReferralRepository) {
    suspend operator fun invoke(): Resource<List<Referral>> {
        val result = repository.getReferralHistory()
        val response = result.data ?: return Resource.Error(result.message ?: "Failed to load referral history")
        return Resource.Success(response.data.orEmpty().map { it.toDomain() })
    }
}
