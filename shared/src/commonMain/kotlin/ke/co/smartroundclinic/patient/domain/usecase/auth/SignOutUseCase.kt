package ke.co.smartroundclinic.patient.domain.usecase.auth

import com.liftric.kvault.KVault
import ke.co.smartroundclinic.patient.common.Constants.KEY_ACCESS_TOKEN
import ke.co.smartroundclinic.patient.common.Constants.KEY_REFRESH_TOKEN
import ke.co.smartroundclinic.patient.domain.repository.AuthRepository
import ke.co.smartroundclinic.patient.domain.repository.UserLocalRepository

class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val secureStorage: KVault,
    private val userLocalRepository: UserLocalRepository,
) {
    suspend operator fun invoke() {
        val refreshToken = secureStorage.string(KEY_REFRESH_TOKEN)
        if (refreshToken != null) {
            authRepository.revokeToken(refreshToken)
        }
        secureStorage.deleteObject(KEY_ACCESS_TOKEN)
        secureStorage.deleteObject(KEY_REFRESH_TOKEN)
        userLocalRepository.clearUser()
    }
}
