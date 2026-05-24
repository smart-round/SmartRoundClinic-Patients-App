package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.domain.model.ArticleCategory
import kotlinx.coroutines.flow.Flow

interface ArticleCategoryLocalRepository {
    fun observeActiveCategories(): Flow<List<ArticleCategory>>
    suspend fun getActiveCategories(): List<ArticleCategory>
    suspend fun upsertCategories(categories: List<ArticleCategory>)
}
