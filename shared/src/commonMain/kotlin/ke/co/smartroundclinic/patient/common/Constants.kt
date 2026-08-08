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
     * A ceiling this high is only safe because the upload streams: the file goes straight to
     * R2 via a pre-signed PUT, chunk by chunk, so neither this process nor the API ever holds
     * it in memory. Never read a file this large into a ByteArray.
     *
     * Expressed in decimal MB deliberately — file managers report "26 MB" as 26,000,000 bytes,
     * so a binary-MiB threshold lets files the user calls "26 MB" slip under a "25 MB" cap.
     */
    const val MAX_CHAT_FILE_BYTES = 300L * 1_000_000

    const val FILE_TOO_LARGE_MESSAGE = "Unable to send file as it is too large. Please try again"

    /**
     * Only images below this size get an in-flight thumbnail in the chat bubble, because that
     * preview is the one place we must hold the bytes in memory. Anything larger shows an icon.
     */
    const val MAX_INLINE_PREVIEW_BYTES = 5L * 1_000_000

    /** Chunk size for streaming a file to storage — never load the whole thing. */
    const val UPLOAD_CHUNK_BYTES = 64 * 1024

    /** Uploads are far slower than API calls — the 30s default request timeout is not enough. */
    const val UPLOAD_REQUEST_TIMEOUT_MS = 30L * 60 * 1000
    const val UPLOAD_SOCKET_TIMEOUT_MS = 120L * 1000
}
