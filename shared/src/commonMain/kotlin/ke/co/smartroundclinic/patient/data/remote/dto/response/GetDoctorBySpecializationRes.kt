package ke.co.smartroundclinic.patient.data.remote.dto.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GetDoctorBySpecializationRes(
    val `data`: GetDoctorBySpecializationData,
    val httpStatusCode: Int, // 200
    val message: String, // Success
    val status: Boolean // true
)

@Serializable
data class GetDoctorBySpecializationData(
    val items: List<GetDoctorBySpecializationItem>,
    val page: Int, // 1
    val size: Int, // 20
    val total: Int // 5
)

@Serializable
data class GetDoctorBySpecializationItem(
    val averageRating: Double, // 4.0
    val createdAt: String, // 2026-05-16T18:50:50.490067534Z
    val doctorId: String, // 6a08bc8aea9587f4d53dadb6
    val doctorName: String, // qwert
    val languages: List<JsonElement?>,
    val profileId: String, // 6a08bc8aea9587f4d53dadb8
    val profilePicture: String?, // https://868c9d8e015c6365c5f70beed2b85140.r2.cloudflarestorage.com/smartroundclinic-private/profile-pictures/6a0e365c0cc3f1cb3328330f.jpeg?x-id=GetObject&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ecf53ae43fb6944a55fda7f081e3b1c1%2F20260522%2Fauto%2Fs3%2Faws4_request&X-Amz-Date=20260522T143143Z&X-Amz-Expires=86400&X-Amz-SignedHeaders=host&X-Amz-Signature=09708eab544ff79d5fd9fafc9b9ca09204feffb80f2ab2b644168ad00b6cc75f
    val score: Double, // 0.49463946303571865
    val specializations: List<GetDoctorBySpecializationSpecialization>,
    val totalBookings: Int, // 0
    val totalReviews: Int // 1
)

@Serializable
data class GetDoctorBySpecializationSpecialization(
    val id: String, // 6a08bc8aea9587f4d53dadba
    val specializationId: String, // 69dbb5ac73f215116ff7fdaa
    val specializationName: String // Pediatrics -
)

fun GetDoctorBySpecializationItem.toDomain() = ke.co.smartroundclinic.patient.domain.model.Doctor(
    id = doctorId,
    profileId = profileId,
    name = doctorName.removePrefix("Dr. ").removePrefix("Dr.").removePrefix("Dr ").trim()
        .let { "Dr. $it" },
    profilePicture = profilePicture,
    specialization = specializations.firstOrNull()?.specializationName,
    specializationId = specializations.firstOrNull()?.specializationId,
    facilityName = null,
    averageRating = averageRating,
    totalReviews = totalReviews,
    totalBookings = totalBookings,
    yearsOfExperience = null,
)