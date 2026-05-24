package ke.co.smartroundclinic.patient.domain.usecase.auth

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.SuccessRes
import ke.co.smartroundclinic.patient.domain.repository.AuthRepository

class ResendAccountUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Resource<SuccessRes> =
        repository.resendAccount(email)
}
