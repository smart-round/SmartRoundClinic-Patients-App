package ke.co.smartroundclinic.patient.domain.usecase.auth

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.SuccessRes
import ke.co.smartroundclinic.patient.domain.repository.AuthRepository

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
        gender: String,
        phoneNumber: String,
        dateOfBirth: String,
    ): Resource<SuccessRes> = repository.signUp(
        fullName = fullName,
        email = email,
        password = password,
        gender = gender,
        phoneNumber = phoneNumber,
        dateOfBirth = dateOfBirth,
    )
}
