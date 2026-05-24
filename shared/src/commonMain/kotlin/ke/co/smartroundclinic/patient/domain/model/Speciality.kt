package ke.co.smartroundclinic.patient.domain.model

data class Speciality(
    val id: String,
    val title: String,
    val description: String,
    val color: String,
    val iconUrl: String?,
    val serviceCategoryId: String?,
    val serviceTierId: String?,
)
