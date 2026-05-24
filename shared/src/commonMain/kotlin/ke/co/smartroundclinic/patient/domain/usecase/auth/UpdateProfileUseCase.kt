package ke.co.smartroundclinic.patient.domain.usecase.auth

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.User
import ke.co.smartroundclinic.patient.domain.repository.AuthRepository
import ke.co.smartroundclinic.patient.domain.repository.UserLocalRepository

class UpdateProfileUseCase(
    private val authRepository: AuthRepository,
    private val userLocalRepository: UserLocalRepository,
) {
    suspend operator fun invoke(
        fullName: String? = null,
        email: String? = null,
        phoneNumber: String? = null,
        gender: String? = null,
        dateOfBirth: String? = null,
    ): Resource<User> {
        val result = authRepository.updateProfile(
            fullName = fullName,
            email = email,
            phoneNumber = phoneNumber,
            gender = gender,
            dateOfBirth = dateOfBirth,
        )
        if (result is Resource.Success) {
            result.data?.let { userLocalRepository.saveUser(it) }
        }
        return result
    }
}
