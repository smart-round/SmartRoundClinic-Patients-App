package ke.co.smartroundclinic.patient.domain.usecase.auth

import ke.co.smartroundclinic.patient.data.remote.dto.request.UpdatePasswordReq
import ke.co.smartroundclinic.patient.domain.repository.AuthRepository

class UpdatePasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, newPassword: String, otpCode: String) =
        repository.updatePassword(UpdatePasswordReq(email, newPassword, otpCode))
}
