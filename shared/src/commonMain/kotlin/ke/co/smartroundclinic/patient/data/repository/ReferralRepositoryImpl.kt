package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPendingReferralsRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.ReferralActionRes
import ke.co.smartroundclinic.patient.domain.repository.ReferralRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ReferralRepositoryImpl(private val client: HttpClient) : ReferralRepository {

    override suspend fun getPendingReferrals(): Resource<GetPendingReferralsRes> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("referral/pending").body<GetPendingReferralsRes>()
                Resource.Success(response, response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load referrals")
            }
        }

    override suspend fun acceptReferral(id: String): Resource<ReferralActionRes> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.patch("referral/$id/accept").body<ReferralActionRes>()
                Resource.Success(response, response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to accept referral")
            }
        }

    override suspend fun declineReferral(id: String): Resource<ReferralActionRes> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.patch("referral/$id/decline").body<ReferralActionRes>()
                Resource.Success(response, response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to decline referral")
            }
        }
}
