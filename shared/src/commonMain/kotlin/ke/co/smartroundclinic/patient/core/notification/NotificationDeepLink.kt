package ke.co.smartroundclinic.patient.core.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Bridges the KMPNotifier callback (non-Compose context) with the Compose
 * navigation tree. Set [pending] to true when a push notification is tapped;
 * the first HomeRoot that enters composition will consume it and navigate.
 */
object NotificationDeepLink {
    private val _pending = MutableStateFlow(false)
    val pending = _pending.asStateFlow()

    fun signal() { _pending.value = true }
    fun consume() { _pending.value = false }
}
