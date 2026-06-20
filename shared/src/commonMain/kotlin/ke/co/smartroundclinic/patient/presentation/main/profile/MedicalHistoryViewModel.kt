package ke.co.smartroundclinic.patient.presentation.main.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.MedicalRecord
import ke.co.smartroundclinic.patient.domain.usecase.medicalrecord.GetMyMedicalHistoryUseCase
import kotlinx.coroutines.launch

class MedicalHistoryViewModel(
    private val historyUseCase: GetMyMedicalHistoryUseCase,
) : ViewModel() {

    var records by mutableStateOf<List<MedicalRecord>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            when (val result = historyUseCase()) {
                is Resource.Success -> records = result.data ?: emptyList()
                else -> {}
            }
            isLoading = false
        }
    }
}
