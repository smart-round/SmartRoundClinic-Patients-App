package ke.co.smartroundclinic.patient.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createHttpClient(tokenProvider: () -> String?): HttpClient =
    buildHttpClient(OkHttp.create(), tokenProvider)
