package ke.co.smartroundclinic.patient.core.notification

import com.mmk.kmpnotifier.notification.NotifierManager
import io.github.aakira.napier.Napier
import ke.co.smartroundclinic.patient.domain.usecase.notification.RegisterDeviceTokenUseCase
import ke.co.smartroundclinic.patient.notificationPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val TAG = "NotificationSetup"

fun setupNotificationListener() {
    val scope = CoroutineScope(Dispatchers.IO)
    val component = object : KoinComponent {
        val registerDeviceToken: RegisterDeviceTokenUseCase by inject()
    }

    NotifierManager.addListener(object : NotifierManager.Listener {
        override fun onNewToken(token: String) {
            Napier.d(tag = TAG, message = "FCM token refreshed: $token")
            scope.launch {
                val result = component.registerDeviceToken(token, notificationPlatform)
                Napier.d(tag = TAG, message = "Token registration result (onNewToken): $result")
            }
        }
    })

    // onNewToken only fires when the token changes. Fetch the current token immediately
    // so existing installs always register with the backend on every app start.
    scope.launch {
        val token = NotifierManager.getPushNotifier().getToken()
        Napier.d(tag = TAG, message = "Current FCM token on startup: $token")
        if (token != null) {
            val result = component.registerDeviceToken(token, notificationPlatform)
            Napier.d(tag = TAG, message = "Token registration result (startup): $result")
        } else {
            Napier.w(tag = TAG, message = "FCM token is null on startup — Firebase may not be initialized yet")
        }
    }
}
