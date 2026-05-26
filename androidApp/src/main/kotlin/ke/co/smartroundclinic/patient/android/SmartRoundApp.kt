package ke.co.smartroundclinic.patient.android

import android.app.Application
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import ke.co.smartroundclinic.patient.koin.initKoin
import ke.co.smartroundclinic.patient.logging
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class SmartRoundApp : Application() {
    override fun onCreate() {
        android.util.Log.d("SmartRoundApp", "SmartRoundApp onCreate")
        super.onCreate()
        logging()
        NotifierManager.initialize(
            configuration = NotificationPlatformConfiguration.Android(
                notificationIconResId = R.mipmap.ic_launcher_foreground,
                showPushNotification = true,
            ),
        )
        initKoin(
            appDeclaration = {
                androidLogger()
                androidContext(this@SmartRoundApp)
            },
        )
    }
}
