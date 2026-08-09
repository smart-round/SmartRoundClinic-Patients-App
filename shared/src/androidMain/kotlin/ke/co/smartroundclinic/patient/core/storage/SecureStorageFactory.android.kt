package ke.co.smartroundclinic.patient.core.storage

import android.content.Context
import com.liftric.kvault.KVault
import org.koin.core.context.GlobalContext
import java.security.KeyStore

/** The prefs file KVault uses when it is constructed with `fileName = null`. */
private const val KVAULT_PREFS_FILE = "secure-shared-preferences"

/** Alias of the Keystore key androidx.security's `MasterKey.Builder` creates by default. */
private const val MASTER_KEY_ALIAS = "_androidx_security_master_key_"

actual fun createKVault(): KVault {
    val context = GlobalContext.get().get<Context>()
    return try {
        KVault(context = context, fileName = null)
    } catch (_: Exception) {
        // The encrypted prefs outlived the Keystore key that encrypts them — a restored
        // backup, a reinstall over another account's data, or a signing change. Nothing in
        // there is recoverable, so drop the prefs *and* the master key and start clean.
        resetSecureStorage(context)
        KVault(context = context, fileName = null)
    }
}

private fun resetSecureStorage(context: Context) {
    runCatching { context.deleteSharedPreferences(KVAULT_PREFS_FILE) }
    // The keyset lives in the prefs file, but the master key that seals it lives in the
    // Keystore. Deleting only one of the two leaves the same mismatch behind.
    runCatching {
        KeyStore.getInstance("AndroidKeyStore")
            .apply { load(null) }
            .deleteEntry(MASTER_KEY_ALIAS)
    }
}
