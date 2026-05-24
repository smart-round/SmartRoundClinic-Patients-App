package ke.co.smartroundclinic.patient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ke.co.smartroundclinic.patient.domain.model.ServiceCategory

@Entity(tableName = "service_categories")
data class ServiceCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
)

fun ServiceCategoryEntity.toDomain() = ServiceCategory(id = id, name = name)

fun ServiceCategory.toEntity() = ServiceCategoryEntity(id = id, name = name)
