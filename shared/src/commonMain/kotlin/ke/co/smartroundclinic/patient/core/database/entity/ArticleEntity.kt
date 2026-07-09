package ke.co.smartroundclinic.patient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.model.ArticleState

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val doctorId: String,
    val title: String,
    val content: String,
    val summary: String,
    val categoryId: String,
    val thumbnailUrl: String?,
    val state: String,
    val datePosted: String?,
    val createdAt: String,
    val updatedAt: String?,
    val authorName: String? = null,
)

fun ArticleEntity.toDomain() = Article(
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
    authorName = authorName,
)

fun Article.toEntity() = ArticleEntity(
    id = id,
    doctorId = doctorId,
    title = title,
    content = content,
    summary = summary,
    categoryId = categoryId,
    thumbnailUrl = thumbnailUrl,
    state = state.name,
    datePosted = datePosted,
    createdAt = createdAt,
    updatedAt = updatedAt,
    authorName = authorName,
)
