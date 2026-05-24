package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.model.ArticleCategory

interface ArticleRepository {
    suspend fun getLiveArticles(): Resource<List<Article>>
    suspend fun getCategories(): Resource<List<ArticleCategory>>
}
