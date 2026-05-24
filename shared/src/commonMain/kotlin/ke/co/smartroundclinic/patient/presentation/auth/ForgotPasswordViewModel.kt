package ke.co.smartroundclinic.patient.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.domain.usecase.auth.RequestPasswordResetUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.ResendPasswordResetOtpUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.UpdatePasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val resendPasswordResetOtpUseCase: ResendPasswordResetOtpUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val snackbarController: SnackbarController,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var email = ""
    private var otp = ""

    fun requestPasswordReset(email: String, onSuccess: () -> Unit) {
        this.email = email
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = requestPasswordResetUseCase(email)) {
                is Resource.Success -> onSuccess()
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to send reset code", isError = true)
                is Resource.Loading -> Unit
            }
            _isLoading.value = false
        }
    }

    fun saveOtp(otp: String) {
        this.otp = otp
    }

    fun resendPasswordResetOtp() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = resendPasswordResetOtpUseCase(email)) {
                is Resource.Success -> snackbarController.show(result.message ?: "OTP resent successfully")
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to resend OTP", isError = true)
                is Resource.Loading -> Unit
            }
            _isLoading.value = false
        }
    }

    fun updatePassword(newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = updatePasswordUseCase(email, newPassword, otp)) {
                is Resource.Success -> onSuccess()
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to update password", isError = true)
                is Resource.Loading -> Unit
            }
            _isLoading.value = false
        }
    }

    fun resetState() {
        email = ""
        otp = ""
    }
}
