package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPendingReferralsRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.ReferralActionRes

interface ReferralRepository {
    suspend fun getPendingReferrals(): Resource<GetPendingReferralsRes>
    suspend fun acceptReferral(id: String): Resource<ReferralActionRes>
    suspend fun declineReferral(id: String): Resource<ReferralActionRes>
}
