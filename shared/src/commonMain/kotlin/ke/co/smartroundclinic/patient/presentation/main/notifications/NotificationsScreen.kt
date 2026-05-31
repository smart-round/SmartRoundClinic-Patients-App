package ke.co.smartroundclinic.patient.presentation.main.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.domain.model.Notification
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: NotificationsViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val hasUnread = vm.unreadCount > 0

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(vertical = 4.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center).padding(vertical = 16.dp),
            )
            if (hasUnread) {
                TextButton(
                    onClick = { vm.markAllRead() },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
                ) {
                    Text(
                        text = "Mark all read",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = vm.isLoading,
            onRefresh = { scope.launch { vm.load() } },
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                vm.isLoading && vm.notifications.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                vm.notifications.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize().navigationBarsPadding(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(44.dp),
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            Text(
                                text = "All caught up!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "You have no notifications yet.\nWe'll let you know when something happens.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    ) {
                        if (hasUnread) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${vm.unreadCount} unread",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                        items(vm.notifications, key = { it.id }) { notification ->
                            NotificationCard(
                                notification = notification,
                                onTap = { if (notification.isUnread) vm.markRead(notification.id) },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: Notification, onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (notification.isUnread)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                else
                    Color.Transparent,
            )
            .clickable(onClick = onTap),
    ) {
        // Left accent bar — only for unread
        if (notification.isUnread) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .matchParentSize()
                    .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd))),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (notification.isUnread) 19.dp else 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 14.dp,
                ),
            verticalAlignment = Alignment.Top,
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (notification.isUnread)
                            Brush.linearGradient(listOf(GradientStart, GradientEnd))
                        else
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (notification.isUnread) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = if (notification.isUnread) Color.White
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (notification.isUnread) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (notification.isUnread) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }

                Spacer(Modifier.height(3.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = formatNotificationTime(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

private fun formatNotificationTime(iso: String): String = try {
    val date = iso.substringBefore("T")
    val time = iso.substringAfter("T").substringBefore(".").substringBefore("Z")
    val parts = time.split(":")
    val hour = parts[0].toInt()
    val min = parts.getOrElse(1) { "00" }
    val ampm = if (hour < 12) "AM" else "PM"
    val h = if (hour % 12 == 0) 12 else hour % 12
    "$date · $h:$min $ampm"
} catch (e: Exception) { iso }
