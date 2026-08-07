package ke.co.smartroundclinic.patient.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.ConnectionPool
import java.util.concurrent.TimeUnit

actual fun createHttpClient(tokenProvider: () -> String?): HttpClient =
    buildHttpClient(
        OkHttp.create {
            config {
                // Detect dead pooled connections (e.g. after a WiFi/cellular switch or
                // the device waking from sleep) instead of hanging until socketTimeout.
                pingInterval(15, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
                connectionPool(ConnectionPool(5, 30, TimeUnit.SECONDS))
            }
        },
        tokenProvider,
    )
