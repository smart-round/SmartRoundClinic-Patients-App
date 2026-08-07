package ke.co.smartroundclinic.patient.common

object Constants {
    const val ONBOARDING_KEY = "onboarding_completed"
    const val BASE_URL = "https://sandbox-api.smartroundclinic.co.ke/"
    //const val BASE_URL = "https://api.smartroundclinic.co.ke/"
    const val PRIVACY_POLICY_URL = "https://smartroundclinic.co.ke/privacy-policy/"
    const val TERMS_CONDITIONS_URL = "https://smartroundclinic.co.ke/terms-and-conditions/"
    const val FAQ_URL = "https://smartroundclinic.co.ke/faq/"
    const val KEY_ACCESS_TOKEN = "key_access_token"
    const val KEY_REFRESH_TOKEN = "key_refresh_token"

    /**
     * Largest chat attachment we will attempt to upload.
     *
     * Sized against what the server can actually absorb, not against what a phone can read:
     * the upload endpoint buffers the whole part into memory (`part.provider().toByteArray()`)
     * and only then re-uploads it to R2, so the transfer is serialised twice and the file is
     * resident in server heap throughout. Large files are slow by construction and risk
     * exhausting heap.
     *
     * Expressed in decimal MB deliberately — file managers report "26 MB" as 26,000,000 bytes,
     * so a binary-MiB threshold lets files the user calls "26 MB" slip under a "25 MB" cap.
     */
    const val MAX_CHAT_FILE_BYTES = 10L * 1_000_000

    const val FILE_TOO_LARGE_MESSAGE = "Unable to send file as it is too large. Please try again"

    /** Uploads are far slower than API calls — the 30s default request timeout is not enough. */
    const val UPLOAD_REQUEST_TIMEOUT_MS = 5L * 60 * 1000
    const val UPLOAD_SOCKET_TIMEOUT_MS = 60L * 1000
}
