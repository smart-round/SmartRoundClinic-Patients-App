package ke.co.smartroundclinic.patient.core.presence

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpHeaders
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ke.co.smartroundclinic.patient.common.Constants.BASE_URL

private const val TAG = "PresenceService"
private const val PING_INTERVAL_MS = 25_000L
private const val INITIAL_BACKOFF_MS = 2_000L
private const val MAX_BACKOFF_MS = 30_000L

private val WS_URL = BASE_URL.replace("https://", "wss://") + "auth/user/presence"

@Serializable
private data class PresencePingReq(val isActive: Boolean)

class PresenceService(
    private val client: HttpClient,
    private val tokenProvider: () -> String?,
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun start() {
        if (job?.isActive == true) return
        Napier.d(tag = TAG, message = "Starting presence service — pinging every ${PING_INTERVAL_MS / 1_000}s")
        job = scope.launch { connectWithBackoff() }
    }

    fun stop() {
        Napier.d(tag = TAG, message = "Stopping presence service")
        job?.cancel()
        job = null
    }

    private suspend fun connectWithBackoff() {
        var backoffMs = INITIAL_BACKOFF_MS
        while (true) {
            val token = tokenProvider()
            if (token.isNullOrBlank()) {
                Napier.d(tag = TAG, message = "No auth token yet — retrying in ${backoffMs / 1_000}s")
                delay(backoffMs)
                backoffMs = minOf(backoffMs * 2, MAX_BACKOFF_MS)
                continue
            }
            try {
                client.webSocket(
                    urlString = WS_URL,
                    request = { headers.append(HttpHeaders.Authorization, "Bearer $token") },
                ) {
                    backoffMs = INITIAL_BACKOFF_MS
                    Napier.i(tag = TAG, message = "Presence WebSocket connected")

                    // Drain server acks on a sibling coroutine so the send loop never blocks.
                    launch {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                Napier.v(tag = TAG, message = "Presence ack: ${frame.readText()}")
                            }
                        }
                    }

                    while (isActive) {
                        val ping = json.encodeToString(PresencePingReq(isActive = true))
                        send(Frame.Text(ping))
                        Napier.v(tag = TAG, message = "Sent presence ping")
                        delay(PING_INTERVAL_MS)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.w(
                    tag = TAG,
                    message = "Presence WebSocket disconnected: ${e.message} — reconnecting in ${backoffMs / 1_000}s",
                )
                delay(backoffMs)
                backoffMs = minOf(backoffMs * 2, MAX_BACKOFF_MS)
            }
        }
    }
}
