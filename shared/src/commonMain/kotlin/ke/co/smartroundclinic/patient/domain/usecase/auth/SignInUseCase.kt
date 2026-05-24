package ke.co.smartroundclinic.patient.domain.usecase.auth

import com.liftric.kvault.KVault
import ke.co.smartroundclinic.patient.common.Constants.KEY_ACCESS_TOKEN
import ke.co.smartroundclinic.patient.common.Constants.KEY_REFRESH_TOKEN
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.AuthTokens
import ke.co.smartroundclinic.patient.domain.repository.AuthRepository

class SignInUseCase(
    private val repository: AuthRepository,
    private val secureStorage: KVault,
) {
    suspend operator fun invoke(email: String, password: String): Resource<AuthTokens> {
        val result = repository.signIn(email, password)
        if (result is Resource.Success) {
            result.data?.let { tokens ->
                tokens.accessToken?.let { secureStorage.set(KEY_ACCESS_TOKEN, it) }
                tokens.refreshToken?.let { secureStorage.set(KEY_REFRESH_TOKEN, it) }
            }
        }
        return result
    }
}
