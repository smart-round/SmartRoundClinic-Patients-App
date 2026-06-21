package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.model.ArticleState
import kotlinx.serialization.Serializable

@Serializable
data class GetDoctorArticlesRes(
    val `data`: GetDoctorArticlesData,
    val httpStatusCode: Int, // 200
    val message: String, // Articles retrieved successfully
    val status: Boolean // true
)

@Serializable
data class GetDoctorArticlesData(
    val items: List<GetDoctorArticlesItem>,
    val page: Int, // 1
    val pages: Int, // 1
    val size: Int, // 20
    val total: Int // 1
)

@Serializable
data class GetDoctorArticlesItem(
    val categoryId: String, // 69ecdd878d4853405ca865b9
    val content: String, // Dont use too much salt
    val createdAt: String, // 2026-04-25T17:52:21.257511Z
    val datePosted: String? = null, // 2026-04-25T17:52:53.794148Z
    val doctorId: String, // 69da69d03ca72358d46dcf7c
    val id: String, // 69ecff55c09d71db52617907
    val state: String, // SUSPENDED
    val summary: String, // By keeping low blood pressure you improve your cadio-vascular health
    val thumbnailUrl: String, // https://868c9d8e015c6365c5f70beed2b85140.r2.cloudflarestorage.com/smartroundclinic-private/article-thumbnails/69ecff55c09d71db52617907.jpeg?x-id=GetObject&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ecf53ae43fb6944a55fda7f081e3b1c1%2F20260522%2Fauto%2Fs3%2Faws4_request&X-Amz-Date=20260522T160341Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=63582002d1e5dcd79c6e0e98554f192848b8533321bb0ab9a0b6e3a29d36abfe
    val title: String, // How to maintaing you Blood Pressure
    val updatedAt: String? = null // 2026-04-30T22:06:41.098046733Z
)

fun GetDoctorArticlesItem.toDomain() = Article(
    id = id,
    doctorId = doctorId,
    title = title,
    content = content,
    summary = summary,
    categoryId = categoryId,
    thumbnailUrl = thumbnailUrl,
    state = when (state) {
        "LIVE" -> ArticleState.LIVE
        "SUSPENDED" -> ArticleState.SUSPENDED
        "DELETED" -> ArticleState.DELETED
        else -> ArticleState.DRAFT
    },
    datePosted = datePosted,
    createdAt = createdAt,
    updatedAt = updatedAt,
)