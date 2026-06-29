package ke.co.smartroundclinic.patient.presentation.main.articles.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.model.ArticleCategory
import ke.co.smartroundclinic.patient.presentation.common.composables.PatientDashboardHeader
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.Primary90
import ke.co.smartroundclinic.patient.presentation.theme.ShapeCard
import ke.co.smartroundclinic.patient.presentation.theme.ShapePill

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
    onRefresh: () -> Unit,
    onArticleClick: (Article) -> Unit,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val filteredArticles = if (selectedCategoryId != null) {
        articles.filter { it.categoryId == selectedCategoryId }
    } else articles

    Column(modifier = modifier.fillMaxSize()) {
        PatientDashboardHeader(
            title = "Articles",
            onProfileClick = onProfileClick,
            onNotificationsClick = onNotificationsClick,
        )

        Column(modifier = Modifier.fillMaxSize()) {
            if (categories.isNotEmpty()) {
                CategoryFilterRow(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = onCategorySelected,
                )
            }
            when {
                (isLoading || !hasLoaded) && filteredArticles.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary40)
                    }
                }
                hasLoaded && filteredArticles.isEmpty() -> {
                    EmptyView(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    val pullState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        state = pullState,
                        modifier = Modifier.fillMaxSize(),
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                state = pullState,
                                isRefreshing = isRefreshing,
                                color = Primary40,
                                modifier = Modifier.align(Alignment.TopCenter),
                            )
                        },
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(filteredArticles, key = { it.id }) { article ->
                                ArticleCard(
                                    article = article,
                                    categoryName = categories.find { it.id == article.categoryId }?.name,
                                    onClick = { onArticleClick(article) },
                                )
                            }
                        }
                        if (isLoading && !isRefreshing) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp), color = Primary40)
                            }
                        }
                    }
                }
            }
        }
    }
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(label = "All", isSelected = selectedCategoryId == null, onClick = { onCategorySelected(null) })
        }
        items(categories, key = { it.id }) { cat ->
            FilterChip(label = cat.name, isSelected = selectedCategoryId == cat.id, onClick = { onCategorySelected(cat.id) })
        }
    }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val gradient = Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(ShapePill)
            .then(
                if (isSelected) Modifier.background(gradient)
                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            )
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Primary90), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.AutoMirrored.Outlined.Article, contentDescription = null, tint = Primary40, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(text = "No Articles Yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Health articles will appear here once published",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    categoryName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(ShapeCard)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(Primary40),
        )
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (article.thumbnailUrl != null) {
                        AsyncImage(
                            model = article.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topEnd = 16.dp)),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }

                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (article.summary.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!categoryName.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Primary90)
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = categoryName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = Primary40,
                                )
                            }
                        }
                        if (article.datePosted != null) {
                            Text(
                                text = article.datePosted.take(10),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
