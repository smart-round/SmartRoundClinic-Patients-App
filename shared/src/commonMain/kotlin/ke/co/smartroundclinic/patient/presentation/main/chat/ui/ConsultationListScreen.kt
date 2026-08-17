package ke.co.smartroundclinic.patient.presentation.main.chat.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ke.co.smartroundclinic.patient.domain.model.ConversationThread
import ke.co.smartroundclinic.patient.domain.model.ThreadPreviewKind
import ke.co.smartroundclinic.patient.presentation.common.composables.PatientDashboardHeader
import ke.co.smartroundclinic.patient.presentation.theme.Neutral20
import ke.co.smartroundclinic.patient.presentation.theme.Neutral60
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.Primary90
import ke.co.smartroundclinic.patient.presentation.theme.StatusConfirmed
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// ── Amended chat card (369×78 in the 414pt Figma frame) ──────────────────────
/** 369 wide in a 414 frame — the same 23dp gutter the amended Articles screens hang off. */
private val ChatCardGutter = 23.dp
private val ChatCardHeight = 78.dp
private val ChatCardPadding = 18.dp
private val ChatCardAvatarGap = 31.dp
private const val ChatCardAvatarSize = 53
private val ChatCardShape = RoundedCornerShape(12.dp)

/** #393938 at 3% — a wash just strong enough to separate the card from the page. */
private val ChatCardBackground = Neutral20.copy(alpha = 0.03f)

@Composable
internal fun ConsultationListScreen(
    threads: List<ConversationThread>,
    isLoading: Boolean,
    onThreadClick: (ConversationThread) -> Unit,
    onRefresh: () -> Unit,
    onDeleteThread: (ConversationThread) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var threadPendingDelete by remember { mutableStateOf<ConversationThread?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val visibleThreads = if (searchQuery.isNotBlank()) {
        threads.filter { it.counterpartName.contains(searchQuery, ignoreCase = true) }
    } else {
        threads
    }

    Column(modifier = modifier.fillMaxSize()) {
        PatientDashboardHeader(
            title = "Consultations",
            onProfileClick = onProfileClick,
            onNotificationsClick = onNotificationsClick,
            bottomContent = {
                ChatSearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                )
            },
        )

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (isLoading && threads.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (visibleThreads.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Primary90),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(imageVector = Icons.Filled.ChatBubbleOutline, contentDescription = null, tint = Primary40, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No doctors found" else "No Conversations Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        if (searchQuery.isBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Conversations with your doctors will appear here\nonce you have a confirmed appointment.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    contentPadding = PaddingValues(horizontal = ChatCardGutter, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(visibleThreads, key = { it.threadId }) { thread ->
                        ConsultationThreadCard(
                            thread = thread,
                            onClick = { onThreadClick(thread) },
                            onLongClick = { threadPendingDelete = thread },
                        )
                    }
                }
            }
        }
    }

    threadPendingDelete?.let { thread ->
        AlertDialog(
            onDismissRequest = { threadPendingDelete = null },
            title = { Text("Delete conversation?") },
            text = { Text("This removes your conversation with Dr. ${thread.counterpartName} from this list. It will reappear if they send a new message.") },
            confirmButton = {
                TextButton(onClick = { onDeleteThread(thread); threadPendingDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { threadPendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConsultationThreadCard(thread: ConversationThread, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ChatCardHeight)
            .clip(ChatCardShape)
            .background(ChatCardBackground)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = ChatCardPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            DoctorAvatar(picture = thread.counterpartPicture, size = ChatCardAvatarSize)
            if (thread.isOnline) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(StatusConfirmed),
                )
            }
        }

        Spacer(Modifier.width(ChatCardAvatarGap))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Dr. ${thread.counterpartName}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.sp,
                ),
                color = Neutral20,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(6.dp))

            // Preview and timestamp share the second line in the amended card, rather than the
            // timestamp sitting centred against the full row height.
            Row(verticalAlignment = Alignment.CenterVertically) {
                val previewIcon = when (thread.lastMessageKind) {
                    ThreadPreviewKind.PHOTO -> Icons.Filled.CameraAlt
                    ThreadPreviewKind.VIDEO -> Icons.Filled.Videocam
                    else -> null
                }
                if (previewIcon != null) {
                    Icon(
                        imageVector = previewIcon,
                        contentDescription = null,
                        tint = Neutral60,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = thread.lastMessagePreview ?: "No messages yet",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, letterSpacing = 0.sp),
                    color = Neutral60,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (thread.lastMessageAt != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatThreadTimestamp(thread.lastMessageAt),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, letterSpacing = 0.sp),
                        color = Neutral60,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private fun formatThreadTimestamp(iso: String): String = try {
    val zone = TimeZone.currentSystemDefault()
    val dateTime = Instant.parse(iso).toLocalDateTime(zone)
    val today = Clock.System.now().toLocalDateTime(zone).date
    if (dateTime.date == today) {
        val hour = dateTime.hour
        val ampm = if (hour < 12) "AM" else "PM"
        val h = if (hour % 12 == 0) 12 else hour % 12
        "$h:${dateTime.minute.toString().padStart(2, '0')} $ampm"
    } else {
        "${dateTime.date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${dateTime.date.dayOfMonth}"
    }
} catch (_: Exception) { "" }

@Composable
internal fun DoctorAvatar(picture: String?, size: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size.dp).clip(CircleShape).background(Primary90),
        contentAlignment = Alignment.Center,
    ) {
        if (!picture.isNullOrBlank()) {
            AsyncImage(
                model = picture,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Primary40,
                modifier = Modifier.size((size * 0.6).dp),
            )
        }
    }
}

/**
 * The Services tab's in-header search field, with the magnifier moved to the trailing edge — it
 * doubles as the clear button once there is a query to clear.
 */
@Composable
private fun ChatSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search doctors...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
            )
        },
        trailingIcon = {
            if (query.isEmpty()) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp),
                )
            } else {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear search",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White.copy(alpha = 0.6f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
            cursorColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.15f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = modifier.fillMaxWidth(),
    )
}
