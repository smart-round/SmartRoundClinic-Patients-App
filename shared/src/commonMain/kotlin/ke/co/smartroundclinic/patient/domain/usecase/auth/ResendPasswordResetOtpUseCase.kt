package ke.co.smartroundclinic.patient.domain.usecase.auth

import ke.co.smartroundclinic.patient.domain.repository.AuthRepository

class ResendPasswordResetOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String) = repository.resendPasswordResetOtp(email)
}
