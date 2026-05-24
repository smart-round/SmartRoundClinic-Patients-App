package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.ServiceCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetServiceCategoriesRes(
    val `data`: ServiceCategoryData,
    val httpStatusCode: Int, // 200
    val message: String, // Service categories retrieved successfully
    val status: Boolean // true
)

@Serializable
data class ServiceCategoryData(
    val items: List<ServiceCategoryItem>,
    val page: Int, // 1
    val pages: Int, // 1
    val size: Int, // 20
    val total: Int // 4
)

@Serializable
data class ServiceCategoryItem(
    val createdAt: String, // 2026-04-25T13:00:06.811692Z
    val id: String, // 69ecbad602b9ad21f57f30ae
    val name: String // Family Medicine
)

fun ServiceCategoryItem.toDomain() = ServiceCategory(id = id, name = name)