package ke.co.smartroundclinic.patient.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import ke.co.smartroundclinic.patient.domain.repository.DatastoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DatastoreRepositoryImpl(
    private val datastore: DataStore<Preferences>
) : DatastoreRepository {

    override fun observe(key: String): Flow<String?> {
        return datastore.data.map { prefs -> prefs[stringPreferencesKey(key)] }
    }

    override suspend fun setKey(key: String, value: String?): Boolean {
        return runCatching {
            datastore.edit { prefs ->
                if (value != null) prefs[stringPreferencesKey(key)] = value
                else prefs.remove(stringPreferencesKey(key))
            }
        }.isSuccess
    }

    override suspend fun getKey(key: String): String? {
        return datastore.data.firstOrNull()?.get(stringPreferencesKey(key))
    }
}
