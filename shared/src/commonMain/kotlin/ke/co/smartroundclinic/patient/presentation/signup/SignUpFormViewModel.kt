package ke.co.smartroundclinic.patient.presentation.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.domain.usecase.auth.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignUpFormViewModel(
    private val signUpUseCase: SignUpUseCase,
    private val snackbarController: SnackbarController,
) : ViewModel() {

    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var agreedToTerms by mutableStateOf(false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun signUp(profilePictureBytes: ByteArray?, onSuccess: (email: String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = signUpUseCase(
                fullName = fullName,
                email = email,
                password = password,
                profilePictureBytes = profilePictureBytes,
            )) {
                is Resource.Success -> {
                    snackbarController.show(result.message ?: "Account created! Please verify your email.")
                    onSuccess(email)
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Sign up failed", isError = true)
                is Resource.Loading -> Unit
            }
            _isLoading.value = false
        }
    }
}
