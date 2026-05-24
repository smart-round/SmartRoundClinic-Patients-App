package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.DoctorProfile

import kotlinx.serialization.Serializable

@Serializable
data class GetDoctorsProfileRes(
    val `data`: GetDoctorsProfileData,
    val httpStatusCode: Int, // 200
    val message: String, // Success
    val status: Boolean // true
)

@Serializable
data class GetDoctorsProfileData(
    val bio: String? = null, // Experienced general practitioner with a passion for patient-centered care.
    val createdAt: String, // 2026-04-19T14:21:40.294387Z
    val doctorId: String, // 69da69d03ca72358d46dcf7c
    val facilityName: String? = null, // Nairobi Medical Centre
    val id: String, // 69de4cbcc4474c982776d9ba
    val kmpdcRegNumber: String? = null, // KMPDC/2015/12345
    val languages: List<String>,
    val title: String? = null, // Dr.
    val yearsOfExperience: Int? = null // 8
)


fun GetDoctorsProfileData.toDomain() = DoctorProfile(
    id = id,
    doctorId = doctorId,
    bio = bio,
    facilityName = facilityName,
    kmpdcRegNumber = kmpdcRegNumber,
    languages = languages,
    title = title,
    yearsOfExperience = yearsOfExperience,
)