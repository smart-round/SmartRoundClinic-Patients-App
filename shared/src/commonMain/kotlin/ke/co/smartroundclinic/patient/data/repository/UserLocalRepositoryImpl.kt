package ke.co.smartroundclinic.patient.data.repository

import ke.co.smartroundclinic.patient.core.database.dao.UserDao
import ke.co.smartroundclinic.patient.core.database.entity.toDomain
import ke.co.smartroundclinic.patient.core.database.entity.toEntity
import ke.co.smartroundclinic.patient.domain.model.User
import ke.co.smartroundclinic.patient.domain.repository.UserLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserLocalRepositoryImpl(private val dao: UserDao) : UserLocalRepository {

    override fun observeUser(): Flow<User?> = dao.observeUser().map { it?.toDomain() }

    override suspend fun saveUser(user: User) = dao.upsertUser(user.toEntity())

    override suspend fun clearUser() = dao.clearUser()
}
