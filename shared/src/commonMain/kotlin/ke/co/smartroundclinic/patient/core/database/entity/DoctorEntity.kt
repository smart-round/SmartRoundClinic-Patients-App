package ke.co.smartroundclinic.patient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ke.co.smartroundclinic.patient.domain.model.Doctor

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey val id: String,
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

fun DoctorEntity.toDomain() = Doctor(
    id = id,
    profileId = profileId,
    name = name,
    profilePicture = profilePicture,
    specialization = specialization,
    specializationId = specializationId,
    facilityName = facilityName,
    averageRating = averageRating,
    totalReviews = totalReviews,
    totalBookings = totalBookings,
    yearsOfExperience = yearsOfExperience,
)

fun Doctor.toEntity() = DoctorEntity(
    id = id,
    profileId = profileId,
    name = name,
    profilePicture = profilePicture,
    specialization = specialization,
    specializationId = specializationId,
    facilityName = facilityName,
    averageRating = averageRating,
    totalReviews = totalReviews,
    totalBookings = totalBookings,
    yearsOfExperience = yearsOfExperience,
)
