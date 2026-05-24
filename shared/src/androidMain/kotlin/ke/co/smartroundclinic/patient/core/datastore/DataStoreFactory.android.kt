package ke.co.smartroundclinic.patient.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toOkioPath
import org.koin.core.context.GlobalContext

actual fun createDataStore(): DataStore<Preferences> {
    val context = GlobalContext.get().get<Context>()
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { context.filesDir.resolve(PREFERENCES_FILE).toOkioPath() }
    )
}
