package ke.co.smartroundclinic.patient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ke.co.smartroundclinic.patient.domain.model.ArticleCategory

@Entity(tableName = "article_categories")
data class ArticleCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isActive: Boolean,
)

fun ArticleCategoryEntity.toDomain() = ArticleCategory(id = id, name = name, isActive = isActive)

fun ArticleCategory.toEntity() = ArticleCategoryEntity(id = id, name = name, isActive = isActive)
