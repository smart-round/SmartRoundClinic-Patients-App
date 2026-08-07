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
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin qualifier for the auth-free client used to PUT to pre-signed storage URLs. */
const val STORAGE_HTTP_CLIENT = "storageHttpClient"

val coreModule = module {
    single<AppDatabase> { createDatabase() }
    single { createKVault() }
    single { createDataStore() }
    single { SnackbarController() }
    single {
        val kvault = get<KVault>()
        createHttpClient { kvault.string(KEY_ACCESS_TOKEN) }
    }
    // Separate client for pre-signed storage PUTs. It must send **no** Authorization header:
    // the credentials are baked into the pre-signed URL, and an extra auth header makes S3/R2
    // reject the request as a signature mismatch.
    single(named(STORAGE_HTTP_CLIENT)) { createHttpClient { null } }
    single {
        val kvault = get<KVault>()
        PresenceService(get()) { kvault.string(KEY_ACCESS_TOKEN) }
    }
}
