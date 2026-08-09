package ke.co.smartroundclinic.patient.presentation.main.articles.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.model.ArticleCategory
import ke.co.smartroundclinic.patient.presentation.main.articles.formatDayMonth
import ke.co.smartroundclinic.patient.presentation.main.articles.readMinutes
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Neutral20
import ke.co.smartroundclinic.patient.presentation.theme.Neutral60
import ke.co.smartroundclinic.patient.presentation.theme.Primary40

// ── Figma geometry (414pt frame) ─────────────────────────────────────────────
private val CardShape = RoundedCornerShape(12.dp)
private val CardHeight = 129.dp
private val CardThumbWidth = 97.dp
private val ChipHeight = 21.dp

/** Card fill — #E84E1C21 from the spec (brand orange at 0x21 alpha). */
private val CardBackground = Color(0x21E84E1C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ArticleListScreen(
    articles: List<Article>,
    categories: List<ArticleCategory>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    isLoading: Boolean,
    hasLoaded: Boolean,
    isRefreshing: Boolean,
    isSearching: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchingChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onArticleClick: (Article) -> Unit,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val filteredArticles = remember(articles, selectedCategoryId, query) {
        articles
            .filter { selectedCategoryId == null || it.categoryId == selectedCategoryId }
            .filter { article ->
                query.isBlank() ||
                    article.title.contains(query, ignoreCase = true) ||
                    article.summary.contains(query, ignoreCase = true) ||
                    (article.authorName?.contains(query, ignoreCase = true) == true)
            }
    }

    // Without this, switching category leaves the list scrolled where the previous one was, so the
    // first card of the new selection opens hidden under the filter chips.
    val listState = rememberLazyListState()
    LaunchedEffect(selectedCategoryId) { listState.scrollToItem(0) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                ArticlesHeader(
                    title = if (isSearching) null else "Articles",
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick,
                    onSearchClick = { onSearchingChange(!isSearching) },
                )

                if (isSearching) {
                    ArticleSearchField(
                        query = query,
                        onQueryChange = onQueryChange,
                        onClose = { onSearchingChange(false) },
                    )
                }

                if (categories.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    CategoryFilterRow(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = onCategorySelected,
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        val pullState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    color = Primary40,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            },
        ) {
            when {
                (isLoading || !hasLoaded) && filteredArticles.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary40)
                    }
                }

                hasLoaded && filteredArticles.isEmpty() -> {
                    EmptyArticlesView(
                        isFiltered = query.isNotBlank() || selectedCategoryId != null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = ArticlesGutter,
                            end = ArticlesGutter,
                            top = 22.dp,
                            bottom = 24.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(filteredArticles, key = { it.id }) { article ->
                            ArticleCard(
                                article = article,
                                authorName = article.authorName.orEmpty(),
                                onClick = { onArticleClick(article) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search articles", style = MaterialTheme.typography.bodyMedium, color = Neutral60) },
        singleLine = true,
        shape = CardShape,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close search",
                tint = Neutral60,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClose,
                    ),
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary40,
            unfocusedBorderColor = Primary40,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ArticlesGutter, vertical = 12.dp)
            .focusRequester(focusRequester),
    )
}

@Composable
private fun CategoryFilterRow(
    categories: List<ArticleCategory>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = ArticlesGutter),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            CategoryChip(
                label = "All",
                isSelected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
            )
        }
        items(categories, key = { it.id }) { category ->
            CategoryChip(
                label = category.name,
                isSelected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val gradient = Brush.verticalGradient(listOf(GradientStart, GradientEnd))
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(ChipHeight)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier.background(gradient)
                } else {
                    Modifier
                        .background(Color.White)
                        .border(1.dp, Neutral20.copy(alpha = 0.17f), CircleShape)
                },
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 14.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, letterSpacing = 0.sp),
            color = if (isSelected) Color.White else Neutral20,
            maxLines = 1,
        )
    }
}

@Composable
private fun EmptyArticlesView(isFiltered: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Primary40.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Article,
                contentDescription = null,
                tint = Primary40,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = if (isFiltered) "No Matching Articles" else "No Articles Yet",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            ),
            color = Neutral20,
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = if (isFiltered) {
                "Try a different search or category"
            } else {
                "Health articles will appear here once published"
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = Neutral60,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp),
        )
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    authorName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val minutes = remember(article.content) { readMinutes(article.content) }
    val formattedDate = remember(article.datePosted, article.createdAt) {
        formatDayMonth(article.datePosted ?: article.createdAt)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CardHeight)
            .clip(CardShape)
            .background(CardBackground)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 12.dp),
            ) {
                Text(
                    text = buildAnnotatedString {
                        if (authorName.isNotBlank()) {
                            withStyle(SpanStyle(color = Primary40, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)) {
                                append(authorName)
                            }
                            append("  ")
                        }
                        withStyle(SpanStyle(color = Neutral60, fontSize = 9.sp)) {
                            append("· $minutes min read")
                            if (formattedDate.isNotBlank()) append(" · $formattedDate")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.sp,
                    ),
                    color = Neutral20,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        letterSpacing = 0.sp,
                    ),
                    color = Neutral60,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 7.dp, end = 5.dp)
                    .width(CardThumbWidth)
                    .fillMaxHeight()
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (article.thumbnailUrl != null) {
                    AsyncImage(
                        model = article.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp).align(Alignment.Center),
                    )
                }
            }
        }
    }
}
