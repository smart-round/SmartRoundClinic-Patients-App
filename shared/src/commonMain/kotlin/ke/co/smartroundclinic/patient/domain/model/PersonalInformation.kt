package ke.co.smartroundclinic.patient.domain.model

data class PersonalInformation(
    val weight: Double?,
    val weightIn: String?,
    val height: Double?,
    val heightIn: String?,
    val bloodGroup: String?,
    val maritalStatus: String?,
    val allergies: List<String>,
    val chronicConditions: List<String>,
    val currentMedications: List<String>,
)
