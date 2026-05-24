package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserLocalRepository {
    fun observeUser(): Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun clearUser()
}
