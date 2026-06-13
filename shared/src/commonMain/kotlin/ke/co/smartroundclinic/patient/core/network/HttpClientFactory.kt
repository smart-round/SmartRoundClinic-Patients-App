package ke.co.smartroundclinic.patient.core.network

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import ke.co.smartroundclinic.patient.common.Constants.BASE_URL
import kotlinx.serialization.json.Json

internal fun buildHttpClient(engine: HttpClientEngine, tokenProvider: () -> String? = { null }): HttpClient = HttpClient(engine) {
    install(WebSockets)
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        })
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Napier.d(tag = "HTTP Client", message = message)
            }
        }
        level = LogLevel.ALL
    }

    install(ResponseObserver) {
        onResponse { response ->
            if (response.status.value !in 200..299) {
                Napier.e(tag = "HTTP Error", message = "Request failed: ${response.call.request.method.value} ${response.call.request.url} - Status: ${response.status}")
            } else {
                Napier.i(tag = "HTTP Success", message = "Request success: ${response.call.request.method.value} ${response.call.request.url} - Status: ${response.status}")
            }
        }
    }

    install(HttpRequestRetry) {
        maxRetries = 3
        retryIf { request, response ->
            request.method == HttpMethod.Get && response.status.value in 500..599
        }
        retryOnExceptionIf { request, _ ->
            request.method == HttpMethod.Get
        }
        exponentialDelay(
            base = 2.0,
            maxDelayMs = 15_000L,
            randomizationMs = 1_000L
        )
        modifyRequest {
            Napier.w(tag = "HTTP Retry", message = "Retrying request: ${it.url.buildString()}")
        }
    }

    defaultRequest {
        url(BASE_URL)
        contentType(ContentType.Application.Json)
        tokenProvider()?.let { token ->
            headers.append("Authorization", "Bearer $token")
        }
    }
}

expect fun createHttpClient(tokenProvider: () -> String?): HttpClient
