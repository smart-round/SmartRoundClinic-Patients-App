package ke.co.smartroundclinic.patient.domain.usecase.articles

import io.github.aakira.napier.Napier
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.repository.ArticleCategoryLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.ArticleRepository

class GetArticleCategoriesUseCase(
    private val remote: ArticleRepository,
    private val local: ArticleCategoryLocalRepository,
) {
    suspend operator fun invoke(forceRefresh: Boolean = false) {
        if (forceRefresh || local.getActiveCategories().isEmpty()) {
            val result = remote.getCategories()
            when (result) {
                is Resource.Success -> result.data?.let { local.upsertCategories(it) }
                is Resource.Error -> Napier.e("GetArticleCategoriesUseCase: ${result.message}")
                else -> Unit
            }
        }
    }
}
