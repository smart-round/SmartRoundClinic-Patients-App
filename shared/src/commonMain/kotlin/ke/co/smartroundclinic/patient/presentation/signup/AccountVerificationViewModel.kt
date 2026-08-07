package ke.co.smartroundclinic.patient.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.domain.usecase.auth.ResendAccountUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.VerifyAccountUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val COOLDOWN_SECONDS = 60

class AccountVerificationViewModel(
    private val verifyAccountUseCase: VerifyAccountUseCase,
    private val resendAccountUseCase: ResendAccountUseCase,
    private val snackbarController: SnackbarController,
) : ViewModel() {

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying = _isVerifying.asStateFlow()

    private val _isResending = MutableStateFlow(false)
    val isResending = _isResending.asStateFlow()

    private val _resendCooldown = MutableStateFlow(COOLDOWN_SECONDS)
    val resendCooldown = _resendCooldown.asStateFlow()

    private var cooldownJob: Job? = null
    private var hasSentInitialCode = false

    /**
     * Fires once per screen entry (guarded by [hasSentInitialCode]) so navigating here — either
     * right after signup or when an unverified login gets redirected here — always requests a
     * fresh OTP rather than relying on one that may already be stale or was never sent.
     */
    fun sendInitialCode(email: String) {
        if (hasSentInitialCode) return
        hasSentInitialCode = true
        viewModelScope.launch {
            _isResending.value = true
            startCooldown()
            when (val result = resendAccountUseCase(email)) {
                is Resource.Success -> Unit
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to send verification code", isError = true)
                is Resource.Loading -> Unit
            }
            _isResending.value = false
        }
    }

    fun verify(email: String, otpCode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isVerifying.value = true
            when (val result = verifyAccountUseCase(email, otpCode)) {
                is Resource.Success -> {
                    snackbarController.show(result.data?.message ?: "Account verified successfully")
                    onSuccess()
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Verification failed", isError = true)
                is Resource.Loading -> Unit
            }
            _isVerifying.value = false
        }
    }

    fun resend(email: String) {
        if (_resendCooldown.value > 0 || _isResending.value) return
        viewModelScope.launch {
            _isResending.value = true
            startCooldown()
            when (val result = resendAccountUseCase(email)) {
                is Resource.Success -> snackbarController.show(result.data?.message ?: "OTP resent to your email")
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to resend code", isError = true)
                is Resource.Loading -> Unit
            }
            _isResending.value = false
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (remaining in COOLDOWN_SECONDS downTo 0) {
                _resendCooldown.value = remaining
                if (remaining > 0) delay(1_000L)
            }
        }
    }
}
