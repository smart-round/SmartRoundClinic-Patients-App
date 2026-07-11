package ke.co.smartroundclinic.patient.core.notification

import io.github.aakira.napier.Napier
import ke.co.smartroundclinic.patient.domain.usecase.consultation.CancelCallUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.DeclineCallUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Fire-and-forget entry points for native call UI to decline/cancel a ringing call. Android's
 * IncomingCallActivity is already Kotlin and calls the suspend use cases directly; iOS's
 * CXProviderDelegate is Swift and can't call suspend functions across the K/N boundary, so its
 * CXEndCallAction handler goes through here instead.
 */
object CallActionDispatcher : KoinComponent {
    private const val TAG = "CallActionDispatcher"
    private val declineCallUseCase: DeclineCallUseCase by inject()
    private val cancelCallUseCase: CancelCallUseCase by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun decline(otherUserId: String, callId: String) {
        scope.launch {
            val result = declineCallUseCase(otherUserId, callId)
            Napier.d(tag = TAG, message = "Decline result: $result")
        }
    }

    fun cancel(otherUserId: String, callId: String) {
        scope.launch {
            val result = cancelCallUseCase(otherUserId, callId)
            Napier.d(tag = TAG, message = "Cancel result: $result")
        }
    }
}
