package ke.co.smartroundclinic.patient.android

import android.app.Application
import ke.co.smartroundclinic.patient.koin.initKoin
import ke.co.smartroundclinic.patient.logging
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class SmartRoundApp : Application() {
    override fun onCreate() {
        android.util.Log.d("SmartRoundApp", "SmartRoundApp onCreate")
        super.onCreate()
        logging()
        initKoin(
            appDeclaration = {
                androidLogger()
                androidContext(this@SmartRoundApp)
            },
        )
    }
}
