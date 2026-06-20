package ke.co.smartroundclinic.patient.domain.model

data class PersonalInformation(
    val weight: Double?,
    val weightUnit: String?,
    val height: Double?,
    val heightUnit: String?,
    val bloodGroup: String?,
    val maritalStatus: String?,
    val allergies: List<String>,
    val chronicConditions: List<String>,
    val currentMedications: List<String>,
)
