package ke.co.smartroundclinic.patient.presentation.main.articles.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ke.co.smartroundclinic.patient.core.platform.rememberShareText
import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.presentation.main.articles.formatLongDate
import ke.co.smartroundclinic.patient.presentation.main.articles.readMinutes
import ke.co.smartroundclinic.patient.presentation.theme.Neutral20
import ke.co.smartroundclinic.patient.presentation.theme.Neutral40
import ke.co.smartroundclinic.patient.presentation.theme.Primary40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ArticleDetailScreen(
    article: Article,
    onBack: () -> Unit,
    // Only the Articles tab has the category list to hand; the doctor-profile entry points open the
    // same reader without the eyebrow line.
    categoryName: String = "",
    onNotificationsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val shareText = rememberShareText()

    val minutes = remember(article.content) { readMinutes(article.content) }
    val formattedDate = remember(article.datePosted, article.createdAt) {
        formatLongDate(article.datePosted ?: article.createdAt)
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
        topBar = {
            ArticlesHeader(
                onBack = onBack,
                onNotificationsClick = onNotificationsClick,
                onSearchClick = onSearchClick,
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ArticlesGutter)
                .navigationBarsPadding(),
        ) {
            Spacer(Modifier.height(32.dp))

            if (categoryName.isNotBlank()) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, letterSpacing = 0.sp),
                    color = Neutral40,
                )
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                ),
                color = Neutral20,
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = buildAnnotatedString {
                        if (formattedDate.isNotBlank()) append("$formattedDate by ")
                        val author = article.authorName
                        if (!author.isNullOrBlank()) {
                            withStyle(SpanStyle(color = Primary40)) { append(author) }
                        }
                        append(" · $minutes min read")
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, letterSpacing = 0.sp),
                    color = Neutral20,
                    modifier = Modifier.weight(1f),
                )

                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share article",
                    tint = Neutral20,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { shareText("${article.title}\n\n${article.summary}") },
                        ),
                )
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(147.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (article.thumbnailUrl != null) {
                    AsyncImage(
                        model = article.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            HtmlText(
                html = article.content,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
