package ke.co.smartroundclinic.patient.domain.model

data class AuthTokens(
    val accessToken: String?,
    val refreshToken: String?,
    val accountStatus: String,
    val verificationStatus: String,
)
