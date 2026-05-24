package ke.co.smartroundclinic.patient.domain.repository

import kotlinx.coroutines.flow.Flow

interface DatastoreRepository {
    fun observe(key: String): Flow<String?>
    suspend fun setKey(key: String, value: String?): Boolean
    suspend fun getKey(key: String): String?
}
