package ke.co.smartroundclinic.patient.presentation.main.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.domain.repository.UserLocalRepository
import ke.co.smartroundclinic.patient.domain.usecase.personalinfo.CreatePersonalInformationUseCase
import ke.co.smartroundclinic.patient.domain.usecase.personalinfo.GetPersonalInformationUseCase
import ke.co.smartroundclinic.patient.domain.usecase.personalinfo.UpdatePersonalInformationUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// This clinic currently only operates in Kenya — the phone number collected at sign-up has no
// separate country-code field, but the backend requires one to create a personal-information record.
private const val DEFAULT_COUNTRY_CODE = "254"

class MedicalBioViewModel(
    private val getPersonalInformationUseCase: GetPersonalInformationUseCase,
    private val createPersonalInformationUseCase: CreatePersonalInformationUseCase,
    private val updatePersonalInformationUseCase: UpdatePersonalInformationUseCase,
    private val userLocalRepository: UserLocalRepository,
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

    // Drives create (POST, no record yet) vs update (PUT, editing an existing record) and the
    // screen's button label. Set from the GET result — never present without first checking it.
    var hasExistingProfile by mutableStateOf(false)
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
                    hasExistingProfile = data != null
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
                is Resource.Error -> {
                    hasExistingProfile = false
                    // "Not found" just means this patient hasn't created a profile yet — expected
                    // for new sign-ups, not a real error worth alarming them with.
                    val notFound = result.message?.contains("not found", ignoreCase = true) == true
                    if (!notFound) snackbarController.show(result.message ?: "Failed to load medical info", isError = true)
                }
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

            val result = if (hasExistingProfile) {
                updatePersonalInformationUseCase(
                    weight = weight.toDoubleOrNull(),
                    weightIn = weightIn.ifBlank { null },
                    height = height.toDoubleOrNull(),
                    heightIn = heightIn.ifBlank { null },
                    bloodGroup = bloodGroup.ifBlank { null },
                    maritalStatus = maritalStatus.ifBlank { null },
                    allergies = allergies.toList(),
                    chronicConditions = chronicConditions.toList(),
                    currentMedications = currentMedications.toList(),
                )
            } else {
                val user = userLocalRepository.observeUser().first()
                val phoneNumber = user?.phoneNumber
                val dateOfBirth = user?.dateOfBirth
                when {
                    user == null -> {
                        snackbarController.show("Could not load your profile. Please try again.", isError = true)
                        null
                    }
                    phoneNumber.isNullOrBlank() || dateOfBirth.isNullOrBlank() -> {
                        snackbarController.show(
                            "Please add your phone number and date of birth in your profile first",
                            isError = true,
                        )
                        null
                    }
                    bloodGroup.isBlank() -> {
                        snackbarController.show("Blood group is required", isError = true)
                        null
                    }
                    else -> createPersonalInformationUseCase(
                        gender = user.gender,
                        phoneNumber = phoneNumber,
                        countryCode = DEFAULT_COUNTRY_CODE,
                        bloodGroup = bloodGroup,
                        dateOfBirth = dateOfBirth,
                        weight = weight.toDoubleOrNull(),
                        weightIn = weightIn.ifBlank { null },
                        height = height.toDoubleOrNull(),
                        heightIn = heightIn.ifBlank { null },
                        maritalStatus = maritalStatus.ifBlank { null },
                        allergies = allergies.toList(),
                        chronicConditions = chronicConditions.toList(),
                        currentMedications = currentMedications.toList(),
                    )
                }
            }

            when (result) {
                is Resource.Success -> {
                    hasExistingProfile = true
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
