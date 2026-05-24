package ke.co.smartroundclinic.patient.domain.usecase.auth

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.User
import ke.co.smartroundclinic.patient.domain.repository.AuthRepository
import ke.co.smartroundclinic.patient.domain.repository.UserLocalRepository

class GetUserUseCase(
    private val authRepository: AuthRepository,
    private val userLocalRepository: UserLocalRepository,
) {
    suspend operator fun invoke(): Resource<User> {
        val result = authRepository.getUser()
        return when (result) {
            is Resource.Success -> {
                val user = result.data?.data?.toDomain()
                user?.let { userLocalRepository.saveUser(it) }
                Resource.Success(data = user, message = result.message ?: "Success")
            }
            is Resource.Error -> Resource.Error(result.message ?: "Failed")
            is Resource.Loading -> Resource.Loading()
        }
    }
}
