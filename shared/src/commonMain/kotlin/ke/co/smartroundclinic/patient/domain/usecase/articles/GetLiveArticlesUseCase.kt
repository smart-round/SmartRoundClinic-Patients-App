package ke.co.smartroundclinic.patient.domain.usecase.articles

import ke.co.smartroundclinic.patient.domain.repository.ArticleLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.ArticleRepository

class GetLiveArticlesUseCase(
    private val remote: ArticleRepository,
    private val local: ArticleLocalRepository,
) {
    suspend operator fun invoke() {
        val result = remote.getLiveArticles()
        result.data?.let { local.upsertLiveArticles(it) }
    }
}
