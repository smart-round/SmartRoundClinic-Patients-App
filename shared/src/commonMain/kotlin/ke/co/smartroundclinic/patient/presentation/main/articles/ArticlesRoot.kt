package ke.co.smartroundclinic.patient.presentation.main.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

    // The amended design keeps the bottom bar on the article reader too, so this tab never reports
    // itself as off-root — the nested NavDisplay still handles back presses on the detail screen.
    SideEffect { onAtRootChanged(true) }

    val liveArticles by viewModel.liveArticles.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var selectedCategoryId by retain { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }

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
                    query = query,
                    onQueryChange = { query = it },
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
                        categoryName = categories.find { it.id == article.categoryId }?.name.orEmpty(),
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            }
        },
    )
}
