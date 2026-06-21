package ke.co.smartroundclinic.patient.koin

import com.liftric.kvault.KVault
import ke.co.smartroundclinic.patient.common.Constants.KEY_ACCESS_TOKEN
import ke.co.smartroundclinic.patient.core.database.AppDatabase
import ke.co.smartroundclinic.patient.core.database.createDatabase
import ke.co.smartroundclinic.patient.core.datastore.createDataStore
import ke.co.smartroundclinic.patient.core.network.createHttpClient
import ke.co.smartroundclinic.patient.core.presence.PresenceService
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.core.storage.createKVault
import org.koin.dsl.module

val coreModule = module {
    single<AppDatabase> { createDatabase() }
    single { createKVault() }
    single { createDataStore() }
    single { SnackbarController() }
    single {
        val kvault = get<KVault>()
        createHttpClient { kvault.string(KEY_ACCESS_TOKEN) }
    }
    single {
        val kvault = get<KVault>()
        PresenceService(get()) { kvault.string(KEY_ACCESS_TOKEN) }
    }
}
