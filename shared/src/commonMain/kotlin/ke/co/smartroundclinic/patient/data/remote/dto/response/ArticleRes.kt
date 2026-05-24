package ke.co.smartroundclinic.patient.data.remote.dto.response

import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.model.ArticleCategory
import ke.co.smartroundclinic.patient.domain.model.ArticleState
import kotlinx.serialization.Serializable

@Serializable
data class ArticleData(
    val id: String,
    val doctorId: String,
    val title: String,
    val content: String,
    val summary: String,
    val categoryId: String,
    val thumbnailUrl: String? = null,
    val state: String,
    val datePosted: String? = null,
    val createdAt: String = "",
    val updatedAt: String? = null,
)

@Serializable
data class ArticleCategoryData(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val createdAt: String = "",
    val updatedAt: String? = null,
)

@Serializable
data class ArticlePaginatedData(
    val items: List<ArticleData>,
    val total: Int = 0,
    val page: Int = 1,
    val size: Int = 20,
    val pages: Int = 1,
)

@Serializable
data class ArticleListRes(val data: ArticlePaginatedData)

@Serializable
data class CategoryPaginatedData(
    val items: List<ArticleCategoryData>,
    val total: Int = 0,
    val page: Int = 1,
    val size: Int = 20,
    val pages: Int = 1,
)

@Serializable
data class CategoryListRes(val data: CategoryPaginatedData)

fun ArticleData.toDomain() = Article(
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

fun ArticleCategoryData.toDomain() = ArticleCategory(id = id, name = name, isActive = isActive)
