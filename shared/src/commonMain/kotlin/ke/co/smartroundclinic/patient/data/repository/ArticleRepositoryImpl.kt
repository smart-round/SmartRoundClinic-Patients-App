package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.ArticleListRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.CategoryListRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.model.ArticleCategory
import ke.co.smartroundclinic.patient.domain.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ArticleRepositoryImpl(private val client: HttpClient) : ArticleRepository {

    override suspend fun getLiveArticles(): Resource<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val res = client.get("article/live") {
                parameter("size", 100)
            }.body<ArticleListRes>()
            Resource.Success(res.data.items.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load articles")
        }
    }

    override suspend fun getCategories(): Resource<List<ArticleCategory>> = withContext(Dispatchers.IO) {
        try {
            val res = client.get("article/categories/all") {
                parameter("size", 100)
            }.body<CategoryListRes>()
            Resource.Success(res.data.items.filter { it.isActive }.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load categories")
        }
    }
}
