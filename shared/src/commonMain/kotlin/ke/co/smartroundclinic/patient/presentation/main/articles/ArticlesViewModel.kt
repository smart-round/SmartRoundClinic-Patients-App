package ke.co.smartroundclinic.patient.presentation.main.articles

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.model.ArticleCategory
import ke.co.smartroundclinic.patient.domain.repository.ArticleCategoryLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.ArticleLocalRepository
import ke.co.smartroundclinic.patient.domain.usecase.articles.GetArticleCategoriesUseCase
import ke.co.smartroundclinic.patient.domain.usecase.articles.GetLiveArticlesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArticlesViewModel(
    private val getLiveArticlesUseCase: GetLiveArticlesUseCase,
    private val getArticleCategoriesUseCase: GetArticleCategoriesUseCase,
    private val articleLocalRepository: ArticleLocalRepository,
    private val categoryLocalRepository: ArticleCategoryLocalRepository,
) : ViewModel() {

    val liveArticles: StateFlow<List<Article>> = articleLocalRepository.observeLiveArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<ArticleCategory>> = categoryLocalRepository.observeActiveCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var isLoading by mutableStateOf(true)
        private set

    var hasLoaded by mutableStateOf(false)
        private set

    init {
        refresh()
        viewModelScope.launch { getArticleCategoriesUseCase() }
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            getLiveArticlesUseCase()
            isLoading = false
            hasLoaded = true
        }
    }
}
