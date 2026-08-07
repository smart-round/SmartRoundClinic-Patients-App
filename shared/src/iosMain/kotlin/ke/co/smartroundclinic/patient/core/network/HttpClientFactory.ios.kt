package ke.co.smartroundclinic.patient.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createHttpClient(tokenProvider: () -> String?): HttpClient =
    buildHttpClient(
        Darwin.create {
            configureSession {
                // Mirror the Ktor-level HttpTimeout values so NSURLSession doesn't sit
                // on a stale connection independently of Ktor's own timeout handling.
                timeoutIntervalForRequest = 30.0
                timeoutIntervalForResource = 30.0
                waitsForConnectivity = false
            }
        },
        tokenProvider,
    )
