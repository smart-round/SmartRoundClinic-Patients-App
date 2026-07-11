package ke.co.smartroundclinic.patient.core.notification

import io.github.aakira.napier.Napier
import ke.co.smartroundclinic.patient.domain.usecase.notification.RegisterDeviceTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Registers iOS PushKit VoIP tokens with the backend. Called directly from Swift's
 * `PKPushRegistryDelegate.pushRegistry(_:didUpdate:for:)` — VoIP tokens never pass through
 * kmpnotifier (PushKit is a separate, native-only channel with no Kotlin-side equivalent of
 * `onNewToken`).
 */
object VoipTokenRegistrar : KoinComponent {
    private const val TAG = "VoipTokenRegistrar"
    private val registerDeviceToken: RegisterDeviceTokenUseCase by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun register(token: String) {
        scope.launch {
            val result = registerDeviceToken(token, "IOS", "VOIP")
            Napier.d(tag = TAG, message = "VoIP token registration result: $result")
        }
    }
}
