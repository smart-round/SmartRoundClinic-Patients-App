package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.RequestRefreshTokenReq
import ke.co.smartroundclinic.patient.data.remote.dto.request.SignInReq
import ke.co.smartroundclinic.patient.data.remote.dto.request.UpdatePasswordReq
import ke.co.smartroundclinic.patient.data.remote.dto.request.UpdateUserReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetUserRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.SignInRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.SuccessRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.refreshToken.RefreshTokenRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.AuthTokens
import ke.co.smartroundclinic.patient.domain.model.User
import ke.co.smartroundclinic.patient.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(private val client: HttpClient) : AuthRepository {

    override suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
        gender: String,
        phoneNumber: String,
        dateOfBirth: String,
        profilePictureBytes: ByteArray?,
    ): Resource<SuccessRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("auth/user/sign-up-with-picture") {
                parameter("role", "PATIENT")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("fullName", fullName)
                            append("email", email)
                            append("password", password)
                            if (gender.isNotBlank()) append("gender", gender)
                            if (phoneNumber.isNotBlank()) append("phoneNumber", phoneNumber)
                            if (dateOfBirth.isNotBlank()) append("dateOfBirth", dateOfBirth)
                            if (profilePictureBytes != null) {
                                append(
                                    key = "file",
                                    value = profilePictureBytes,
                                    headers = Headers.build {
                                        append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"profile.jpg\"")
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                    },
                                )
                            }
                        }
                    )
                )
            }.body<SuccessRes>()
            if (response.status) Resource.Success(data = response, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun verifyAccount(
        email: String,
        otpCode: String,
    ): Resource<SuccessRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("auth/user/account-verification") {
                parameter("email", email)
                parameter("otpCode", otpCode)
            }.body<SuccessRes>()
            if (response.status) Resource.Success(data = response, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun resendAccount(email: String): Resource<SuccessRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("auth/user/account-verification/resend-otp") {
                parameter("email", email)
            }.body<SuccessRes>()
            if (response.status) Resource.Success(data = response, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun signIn(
        email: String,
        password: String,
    ): Resource<AuthTokens> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("auth/user/sign-in") {
                setBody(SignInReq(email = email, password = password))
            }.body<SignInRes>()
            if (!response.status) return@withContext Resource.Error(message = response.message, data = null)
            Resource.Success(data = response.toDomain(), message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun requestPasswordReset(email: String): Resource<SuccessRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("auth/user/password-reset/request") {
                parameter("email", email)
            }.body<SuccessRes>()
            if (response.status) Resource.Success(data = null, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun resendPasswordResetOtp(email: String): Resource<SuccessRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("auth/user/password-reset/resend-otp") {
                parameter("email", email)
            }.body<SuccessRes>()
            if (response.status) Resource.Success(data = null, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun updatePassword(body: UpdatePasswordReq): Resource<SuccessRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("auth/user/password-reset") {
                setBody(body)
            }.body<SuccessRes>()
            if (response.status) Resource.Success(data = null, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun requestRefreshToken(refreshToken: String): Resource<RefreshTokenRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("auth/user/token/refresh") {
                setBody(RequestRefreshTokenReq(refreshToken))
            }.body<RefreshTokenRes>()
            if (response.status) Resource.Success(data = response, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getUser(): Resource<GetUserRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("auth/user").body<GetUserRes>()
            if (response.status) Resource.Success(data = response, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun updateProfile(
        fullName: String?,
        email: String?,
        phoneNumber: String?,
        gender: String?,
        dateOfBirth: String?,
    ): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = client.put("auth/user") {
                setBody(
                    UpdateUserReq(
                        fullName = fullName,
                        email = email,
                        phoneNumber = phoneNumber,
                        gender = gender,
                        dateOfBirth = dateOfBirth,
                    )
                )
            }.body<GetUserRes>()
            if (response.status) Resource.Success(data = response.data.toDomain(), message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun uploadProfilePicture(imageBytes: ByteArray): Resource<SuccessRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("auth/user/profile-picture") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "file",
                                value = imageBytes,
                                headers = Headers.build {
                                    append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"profile.jpg\"")
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                },
                            )
                        }
                    )
                )
            }.body<SuccessRes>()
            if (response.status) Resource.Success(data = response, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun removeProfilePicture(): Resource<SuccessRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("auth/user/profile-picture").body<SuccessRes>()
            if (response.status) Resource.Success(data = response, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun revokeToken(refreshToken: String): Resource<SuccessRes> = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("auth/user/token/revoke") {
                setBody(RequestRefreshTokenReq(refreshToken))
            }.body<SuccessRes>()
            if (response.status) Resource.Success(data = response, message = response.message)
            else Resource.Error(message = response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}
