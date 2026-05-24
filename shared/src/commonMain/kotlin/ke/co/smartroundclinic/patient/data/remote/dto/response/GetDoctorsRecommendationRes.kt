package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.Doctor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetDoctorsRecommendationRes(
    val `data`: DoctorData,
    val httpStatusCode: Int, // 200
    val message: String, // Success
    val status: Boolean // true
)

@Serializable
data class DoctorData(
    val items: List<DoctorItem>,
    val page: Int, // 1
    val size: Int, // 20
    val total: Int // 22
)

@Serializable
data class DoctorItem(
    val averageRating: Double, // 0.0
    val bio: String?, // Experienced general practitioner with a passion for patient-centered care.
    val createdAt: String, // 2026-05-16T17:19:06.364815626Z
    val doctorId: String, // 6a08a70a8751f92f85e39bae
    val doctorName: String?, // pascarl homes
    val facilityName: String?, // Nairobi Medical Centre
    val kmpdcRegNumber: String?, // KMPDC/2015/12345
    val languages: List<String>,
    val profileId: String, // 6a08a70a8751f92f85e39bb0
    val profilePicture: String?,
    val score: Double, // 0.531751732720825
    val specializations: List<Specialization>,
    val title: String?, // Dr.
    val totalBookings: Int, // 5
    val totalReviews: Int, // 0
    val yearsOfExperience: Int? // 8
)

fun DoctorItem.toDomain() = Doctor(
    id = doctorId,
    profileId = profileId,
    name = listOfNotNull(title, doctorName).joinToString(" ").ifBlank { "Unknown Doctor" },
    profilePicture = profilePicture,
    specialization = specializations.firstOrNull()?.specializationName,
    specializationId = specializations.firstOrNull()?.specializationId,
    facilityName = facilityName,
    averageRating = averageRating,
    totalReviews = totalReviews,
    totalBookings = totalBookings,
    yearsOfExperience = yearsOfExperience,
)