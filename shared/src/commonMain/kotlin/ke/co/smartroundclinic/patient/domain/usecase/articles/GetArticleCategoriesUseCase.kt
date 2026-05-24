package ke.co.smartroundclinic.patient.domain.usecase.articles

import ke.co.smartroundclinic.patient.domain.repository.ArticleCategoryLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.ArticleRepository

class GetArticleCategoriesUseCase(
    private val remote: ArticleRepository,
    private val local: ArticleCategoryLocalRepository,
) {
    suspend operator fun invoke() {
        val cached = local.getActiveCategories()
        if (cached.isNotEmpty()) return
        val result = remote.getCategories()
        result.data?.let { local.upsertCategories(it) }
    }
}
