package ke.co.smartroundclinic.patient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ke.co.smartroundclinic.patient.domain.model.Speciality

@Entity(tableName = "specialities")
data class SpecialityEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val color: String,
    val iconUrl: String?,
    val serviceCategoryId: String?,
    val serviceTierId: String?,
)

fun SpecialityEntity.toDomain() = Speciality(
    id = id,
    title = title,
    description = description,
    color = color,
    iconUrl = iconUrl,
    serviceCategoryId = serviceCategoryId,
    serviceTierId = serviceTierId,
)

fun Speciality.toEntity() = SpecialityEntity(
    id = id,
    title = title,
    description = description,
    color = color,
    iconUrl = iconUrl,
    serviceCategoryId = serviceCategoryId,
    serviceTierId = serviceTierId,
)
