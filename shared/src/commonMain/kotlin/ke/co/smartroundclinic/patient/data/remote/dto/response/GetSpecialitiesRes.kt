package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.Speciality
import kotlinx.serialization.Serializable

@Serializable
data class GetSpecialitiesRes(
    val data: List<SpecialityData>,
    val httpStatusCode: Int,
    val message: String,
    val status: Boolean,
)

@Serializable
data class SpecialityData(
    val id: String,
    val title: String,
    val description: String,
    val color: String,
    val iconUrl: String? = null,
    val serviceCategoryId: String? = null,
    val serviceTierId: String? = null,
)

fun SpecialityData.toDomain() = Speciality(
    id = id,
    title = title,
    description = description,
    color = color,
    iconUrl = iconUrl,
    serviceCategoryId = serviceCategoryId,
    serviceTierId = serviceTierId,
)
