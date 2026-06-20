package ke.co.smartroundclinic.patient.presentation.main.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.domain.usecase.personalinfo.GetPersonalInformationUseCase
import ke.co.smartroundclinic.patient.domain.usecase.personalinfo.UpdatePersonalInformationUseCase
import kotlinx.coroutines.launch

class MedicalBioViewModel(
    private val getPersonalInformationUseCase: GetPersonalInformationUseCase,
    private val updatePersonalInformationUseCase: UpdatePersonalInformationUseCase,
    private val snackbarController: SnackbarController,
) : ViewModel() {

    var weight by mutableStateOf("")
    var weightIn by mutableStateOf("KG")
    var height by mutableStateOf("")
    var heightIn by mutableStateOf("CM")
    var bloodGroup by mutableStateOf("")
    var maritalStatus by mutableStateOf("")

    val allergies = mutableStateListOf<String>()
    val chronicConditions = mutableStateListOf<String>()
    val currentMedications = mutableStateListOf<String>()

    var isLoading by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var saveSuccess by mutableStateOf(false)
        private set

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            isLoading = true
            when (val result = getPersonalInformationUseCase()) {
                is Resource.Success -> {
                    val data = result.data
                    if (data != null) {
                        weight = data.weight?.toString() ?: ""
                        weightIn = data.weightIn ?: "KG"
                        height = data.height?.toString() ?: ""
                        heightIn = data.heightIn ?: "CM"
                        bloodGroup = data.bloodGroup ?: ""
                        maritalStatus = data.maritalStatus ?: ""
                        allergies.clear()
                        allergies.addAll(data.allergies)
                        chronicConditions.clear()
                        chronicConditions.addAll(data.chronicConditions)
                        currentMedications.clear()
                        currentMedications.addAll(data.currentMedications)
                    }
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to load medical info", isError = true)
                else -> {}
            }
            isLoading = false
        }
    }

    fun addAllergy(value: String) {
        val trimmed = value.trim()
        if (trimmed.isNotBlank() && !allergies.contains(trimmed)) allergies.add(trimmed)
    }

    fun removeAllergy(value: String) { allergies.remove(value) }

    fun addChronicCondition(value: String) {
        val trimmed = value.trim()
        if (trimmed.isNotBlank() && !chronicConditions.contains(trimmed)) chronicConditions.add(trimmed)
    }

    fun removeChronicCondition(value: String) { chronicConditions.remove(value) }

    fun addMedication(value: String) {
        val trimmed = value.trim()
        if (trimmed.isNotBlank() && !currentMedications.contains(trimmed)) currentMedications.add(trimmed)
    }

    fun removeMedication(value: String) { currentMedications.remove(value) }

    fun save() {
        viewModelScope.launch {
            isSaving = true
            saveSuccess = false
            when (val result = updatePersonalInformationUseCase(
                weight = weight.toDoubleOrNull(),
                weightIn = weightIn.ifBlank { null },
                height = height.toDoubleOrNull(),
                heightIn = heightIn.ifBlank { null },
                bloodGroup = bloodGroup.ifBlank { null },
                maritalStatus = maritalStatus.ifBlank { null },
                allergies = allergies.toList(),
                chronicConditions = chronicConditions.toList(),
                currentMedications = currentMedications.toList(),
            )) {
                is Resource.Success -> {
                    saveSuccess = true
                    snackbarController.show("Medical information saved")
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to save", isError = true)
                else -> {}
            }
            isSaving = false
        }
    }
}
