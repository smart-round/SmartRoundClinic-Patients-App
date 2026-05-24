package ke.co.smartroundclinic.patient.domain.usecase.auth

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.SuccessRes
import ke.co.smartroundclinic.patient.domain.repository.AuthRepository

class VerifyAccountUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, otpCode: String): Resource<SuccessRes> =
        repository.verifyAccount(email, otpCode)
}
