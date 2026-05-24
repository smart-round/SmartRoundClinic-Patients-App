package ke.co.smartroundclinic.patient.data.repository

import ke.co.smartroundclinic.patient.core.database.dao.ArticleCategoryDao
import ke.co.smartroundclinic.patient.core.database.entity.toDomain
import ke.co.smartroundclinic.patient.core.database.entity.toEntity
import ke.co.smartroundclinic.patient.domain.model.ArticleCategory
import ke.co.smartroundclinic.patient.domain.repository.ArticleCategoryLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArticleCategoryLocalRepositoryImpl(private val dao: ArticleCategoryDao) : ArticleCategoryLocalRepository {

    override fun observeActiveCategories(): Flow<List<ArticleCategory>> =
        dao.observeActiveCategories().map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveCategories(): List<ArticleCategory> =
        dao.getActiveCategories().map { it.toDomain() }

    override suspend fun upsertCategories(categories: List<ArticleCategory>) =
        dao.upsertCategories(categories.map { it.toEntity() })
}
