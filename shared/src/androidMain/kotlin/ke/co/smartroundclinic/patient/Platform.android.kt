package ke.co.smartroundclinic.patient

import android.os.Build
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual val notificationPlatform: String = "android"

fun logging() {
    Napier.base(DebugAntilog())
}
