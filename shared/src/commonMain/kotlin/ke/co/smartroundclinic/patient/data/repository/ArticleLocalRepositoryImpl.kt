package ke.co.smartroundclinic.patient.data.repository

import ke.co.smartroundclinic.patient.core.database.dao.ArticleDao
import ke.co.smartroundclinic.patient.core.database.entity.toDomain
import ke.co.smartroundclinic.patient.core.database.entity.toEntity
import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.repository.ArticleLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArticleLocalRepositoryImpl(private val dao: ArticleDao) : ArticleLocalRepository {

    override fun observeLiveArticles(): Flow<List<Article>> =
        dao.observeLiveArticles().map { list -> list.map { it.toDomain() } }

    override suspend fun upsertLiveArticles(articles: List<Article>) {
        if (articles.isNotEmpty()) dao.upsertArticles(articles.map { it.toEntity() })
    }

    override suspend fun clearAll() = dao.clearAll()
}
