package ke.co.smartroundclinic.patient.domain.model

data class Doctor(
    val id: String,
    val profileId: String,
    val name: String,
    val profilePicture: String?,
    val specialization: String?,
    val specializationId: String?,
    val facilityName: String?,
    val averageRating: Double,
    val totalReviews: Int,
    val totalBookings: Int,
    val yearsOfExperience: Int?,
)
