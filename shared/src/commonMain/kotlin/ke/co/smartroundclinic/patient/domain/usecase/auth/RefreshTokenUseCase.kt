package ke.co.smartroundclinic.patient.domain.usecase.auth

import com.liftric.kvault.KVault
import ke.co.smartroundclinic.patient.common.Constants.KEY_ACCESS_TOKEN
import ke.co.smartroundclinic.patient.common.Constants.KEY_REFRESH_TOKEN
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.repository.AuthRepository

class RefreshTokenUseCase(
    private val repository: AuthRepository,
    private val secureStorage: KVault,
) {
    suspend operator fun invoke(): Resource<Unit> {
        val refreshToken = secureStorage.string(KEY_REFRESH_TOKEN)
            ?: return Resource.Error("No session found")

        return when (val result = repository.requestRefreshToken(refreshToken)) {
            is Resource.Success -> {
                result.data?.data?.let { tokens ->
                    secureStorage.set(KEY_ACCESS_TOKEN, tokens.accessToken)
                    secureStorage.set(KEY_REFRESH_TOKEN, tokens.refreshToken)
                }
                Resource.Success(data = Unit)
            }
            is Resource.Error -> Resource.Error(result.message ?: "Session expired")
            is Resource.Loading -> Resource.Loading()
        }
    }
}
