package ke.co.smartroundclinic.patient.presentation.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SignUpFilesViewModel : ViewModel() {
    var profilePictureBytes: ByteArray? by mutableStateOf(null)
}
