package ke.co.smartroundclinic.patient.presentation.main.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.domain.model.User
import ke.co.smartroundclinic.patient.domain.repository.UserLocalRepository
import ke.co.smartroundclinic.patient.domain.usecase.auth.GetUserUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.RemoveProfilePictureUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.SignOutUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.UpdateProfileUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.UploadProfilePictureUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    userLocalRepository: UserLocalRepository,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val removeProfilePictureUseCase: RemoveProfilePictureUseCase,
    private val snackbarController: SnackbarController,
) : ViewModel() {

    val user: StateFlow<User?> = userLocalRepository.observeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var isUpdating by mutableStateOf(false)
        private set

    var isSavingPicture by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch { getUserUseCase() }
    }

    fun updateProfile(
        fullName: String? = null,
        email: String? = null,
        phoneNumber: String? = null,
        gender: String? = null,
        dateOfBirth: String? = null,
    ) {
        viewModelScope.launch {
            isUpdating = true
            when (val result = updateProfileUseCase(fullName, email, phoneNumber, gender, dateOfBirth)) {
                is Resource.Success -> snackbarController.show("Profile updated")
                is Resource.Error -> snackbarController.show(result.message ?: "Update failed", isError = true)
                else -> {}
            }
            isUpdating = false
        }
    }

    fun uploadProfilePicture(bytes: ByteArray) {
        viewModelScope.launch {
            isSavingPicture = true
            when (val result = uploadProfilePictureUseCase(bytes)) {
                is Resource.Success -> {
                    getUserUseCase()
                    snackbarController.show("Profile picture updated")
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Upload failed", isError = true)
                else -> {}
            }
            isSavingPicture = false
        }
    }

    fun removeProfilePicture() {
        viewModelScope.launch {
            isSavingPicture = true
            when (val result = removeProfilePictureUseCase()) {
                is Resource.Success -> {
                    getUserUseCase()
                    snackbarController.show("Profile picture removed")
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to remove picture", isError = true)
                else -> {}
            }
            isSavingPicture = false
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase()
            onDone()
        }
    }
}
