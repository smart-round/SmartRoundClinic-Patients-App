package ke.co.smartroundclinic.patient.presentation.main.articles.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.notification
import ke.co.smartroundclinic.patient.presentation.main.notifications.NotificationsViewModel
import ke.co.smartroundclinic.patient.presentation.main.profile.ProfileViewModel
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Neutral20
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

/** Avatar in the Figma spec: 39×40 with a 7dp radius. */
private val AvatarShape = RoundedCornerShape(7.dp)

/** Horizontal inset every element on the Articles screens hangs off (23dp in the Figma frame). */
internal val ArticlesGutter = 23.dp

/**
 * The Articles tab's own header — white, not the orange
 * [ke.co.smartroundclinic.patient.presentation.common.composables.PatientDashboardHeader] banner the
 * other tabs use. Leading slot is the patient's avatar on the list screen and a back arrow on every
 * sub-screen; trailing slot is always search + notifications.
 */
@Composable
internal fun ArticlesHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSearchClick: (() -> Unit)? = null,
    profileViewModel: ProfileViewModel = koinViewModel(),
    notificationsViewModel: NotificationsViewModel = koinViewModel(),
) {
    val user by profileViewModel.user.collectAsState()
    val unreadCount = notificationsViewModel.unreadCount

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = ArticlesGutter),
    ) {
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Neutral20,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onBack,
                        ),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 39.dp, height = 40.dp)
                        .clip(AvatarShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onProfileClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Drawn underneath so a missing or un-loadable profile picture still leaves a
                    // recognisable avatar rather than an empty grey tile.
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                    val profilePicture = user?.profilePicture
                    if (profilePicture != null) {
                        AsyncImage(
                            model = profilePicture,
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            if (onSearchClick != null) {
                GradientIcon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search articles",
                    onClick = onSearchClick,
                )
                Spacer(Modifier.width(20.dp))
            }

            Box {
                GradientIcon(
                    painter = painterResource(Res.drawable.notification),
                    contentDescription = "Notifications",
                    onClick = onNotificationsClick,
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp),
                    )
                }
            }
        }

        if (title != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 0.sp,
                ),
                color = Neutral20,
            )
        }
    }
}

/**
 * The header glyphs are filled with the brand gradient in Figma rather than a flat tint, so they
 * are drawn as a normal icon and then re-coloured through a `SrcIn` gradient rect.
 */
@Composable
private fun GradientIcon(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    painter: Painter? = null,
) {
    val gradientModifier = modifier
        .size(22.dp)
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick,
        )
        // SrcIn only clips to the glyph if the icon is drawn into a layer of its own — without the
        // offscreen strategy the gradient would blend against everything already on the canvas.
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            val brush = Brush.verticalGradient(listOf(GradientStart, GradientEnd))
            onDrawWithContent {
                drawContent()
                drawRect(brush = brush, blendMode = BlendMode.SrcIn)
            }
        }

    if (imageVector != null) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = Color.Black,
            modifier = gradientModifier,
        )
    } else if (painter != null) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = Color.Black,
            modifier = gradientModifier,
        )
    }
}
