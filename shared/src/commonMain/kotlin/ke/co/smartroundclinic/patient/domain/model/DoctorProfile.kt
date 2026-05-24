package ke.co.smartroundclinic.patient.domain.model

data class DoctorProfile(
    val id: String,
    val doctorId: String,
    val bio: String?,
    val facilityName: String?,
    val kmpdcRegNumber: String?,
    val languages: List<String>,
    val title: String?,
    val yearsOfExperience: Int?,
)

