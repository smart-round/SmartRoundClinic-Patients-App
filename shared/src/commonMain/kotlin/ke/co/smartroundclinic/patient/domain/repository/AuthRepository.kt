package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.UpdatePasswordReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetUserRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.SuccessRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.refreshToken.RefreshTokenRes
import ke.co.smartroundclinic.patient.domain.model.AuthTokens
import ke.co.smartroundclinic.patient.domain.model.User

interface AuthRepository {
    suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
        gender: String,
        phoneNumber: String,
        dateOfBirth: String,
    ): Resource<SuccessRes>
    suspend fun verifyAccount(email: String, otpCode: String): Resource<SuccessRes>
    suspend fun resendAccount(email: String): Resource<SuccessRes>
    suspend fun signIn(email: String, password: String): Resource<AuthTokens>
    suspend fun requestPasswordReset(email: String): Resource<SuccessRes>
    suspend fun resendPasswordResetOtp(email: String): Resource<SuccessRes>
    suspend fun updatePassword(body: UpdatePasswordReq): Resource<SuccessRes>
    suspend fun requestRefreshToken(refreshToken: String): Resource<RefreshTokenRes>
    suspend fun getUser(): Resource<GetUserRes>
    suspend fun updateProfile(
        fullName: String? = null,
        email: String? = null,
        phoneNumber: String? = null,
        gender: String? = null,
        dateOfBirth: String? = null,
    ): Resource<User>
    suspend fun uploadProfilePicture(imageBytes: ByteArray): Resource<SuccessRes>
    suspend fun removeProfilePicture(): Resource<SuccessRes>
    suspend fun revokeToken(refreshToken: String): Resource<SuccessRes>
}
