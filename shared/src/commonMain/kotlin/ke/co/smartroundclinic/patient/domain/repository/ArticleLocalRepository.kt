package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface ArticleLocalRepository {
    fun observeLiveArticles(): Flow<List<Article>>
    suspend fun upsertLiveArticles(articles: List<Article>)
    suspend fun clearAll()
}
