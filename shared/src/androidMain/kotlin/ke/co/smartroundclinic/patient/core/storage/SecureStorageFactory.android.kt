package ke.co.smartroundclinic.patient.core.storage

import android.content.Context
import com.liftric.kvault.KVault
import org.koin.core.context.GlobalContext

actual fun createKVault(): KVault {
    val context = GlobalContext.get().get<Context>()
    return KVault(context = context, fileName = null)
}
