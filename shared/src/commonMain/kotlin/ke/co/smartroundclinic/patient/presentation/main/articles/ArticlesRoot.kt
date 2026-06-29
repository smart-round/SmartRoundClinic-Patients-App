package ke.co.smartroundclinic.patient.presentation.main.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.presentation.main.articles.destinations.ArticleDetail
import ke.co.smartroundclinic.patient.presentation.main.articles.destinations.ArticleList
import ke.co.smartroundclinic.patient.presentation.main.articles.ui.ArticleDetailScreen
import ke.co.smartroundclinic.patient.presentation.main.articles.ui.ArticleListScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArticlesRoot(
    modifier: Modifier = Modifier,
    onAtRootChanged: (Boolean) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    val viewModel = koinViewModel<ArticlesViewModel>()
    val backStack = retain { mutableStateListOf<NavKey>(ArticleList) }
    val isAtRoot = backStack.size == 1

    SideEffect { onAtRootChanged(isAtRoot) }

    val liveArticles by viewModel.liveArticles.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var selectedCategoryId by retain { mutableStateOf<String?>(null) }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<ArticleList> {
                ArticleListScreen(
                    articles = liveArticles,
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { selectedCategoryId = it },
                    isLoading = viewModel.isLoading,
                    hasLoaded = viewModel.hasLoaded,
                    isRefreshing = viewModel.isRefreshing,
                    onRefresh = { viewModel.pullRefresh() },
                    onArticleClick = { article -> backStack.add(ArticleDetail(article.id)) },
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick,
                )
            }
            entry<ArticleDetail> { dest ->
                val article = liveArticles.find { it.id == dest.articleId }
                if (article != null) {
                    ArticleDetailScreen(
                        article = article,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            }
        },
    )
}
