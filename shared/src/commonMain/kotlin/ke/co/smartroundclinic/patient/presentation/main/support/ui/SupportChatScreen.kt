package ke.co.smartroundclinic.patient.presentation.main.support.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import ke.co.smartroundclinic.patient.domain.model.SupportChatFile
import ke.co.smartroundclinic.patient.domain.model.SupportChatMessage
import ke.co.smartroundclinic.patient.presentation.main.support.SupportChatViewModel
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.Primary90
import kotlinx.coroutines.launch

@Composable
internal fun SupportChatScreen(
    ticketNumber: String,
    currentUserId: String,
    vm: SupportChatViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var viewerFile by remember { mutableStateOf<SupportChatFile?>(null) }

    LaunchedEffect(vm.messages.size) {
        if (vm.messages.isNotEmpty()) listState.animateScrollToItem(vm.messages.lastIndex)
    }

    val filePicker = rememberFilePickerLauncher(type = FileKitType.File()) { file ->
        file?.let { pf ->
            scope.launch {
                val bytes = pf.readBytes()
                val fileName = pf.name
                val ct = pf.mimeType()?.toString() ?: when (fileName.substringAfterLast(".").lowercase()) {
                    "jpg", "jpeg" -> "image/jpeg"
                    "png" -> "image/png"
                    "pdf" -> "application/pdf"
                    "webp" -> "image/webp"
                    else -> "application/octet-stream"
                }
                vm.uploadFile(bytes, fileName, ct)
            }
        }
    }

    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0)) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                    .statusBarsPadding()
                    .padding(vertical = 4.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(modifier = Modifier.align(Alignment.Center).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(ticketNumber, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text(if (vm.isConnected) "Connected" else "Connecting…", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = if (vm.isConnected) 0.9f else 0.6f))
                }
            }

            if (vm.isLoadingHistory) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary40)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    items(vm.messages, key = { it.id }) { message ->
                        ChatBubble(message = message, isOwn = message.senderId == currentUserId, onFileClick = { viewerFile = it })
                    }
                    if (vm.isUploading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Primary40)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                IconButton(onClick = { filePicker.launch() }) {
                    Icon(Icons.Outlined.AttachFile, contentDescription = "Attach file", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type a message…") },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary40, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant),
                )
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = { vm.sendMessage(inputText); inputText = "" }, enabled = inputText.isNotBlank()) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send", tint = if (inputText.isNotBlank()) Primary40 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                }
            }
        }
    }

    viewerFile?.let { file ->
        FileViewerSheet(file = file, onDismiss = { viewerFile = null })
    }
}

@Composable
private fun ChatBubble(
    message: SupportChatMessage,
    isOwn: Boolean,
    onFileClick: (SupportChatFile) -> Unit,
) {
    val bgColor = if (isOwn) Primary40 else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOwn) Color.White else MaterialTheme.colorScheme.onSurface
    val alignment = if (isOwn) Alignment.End else Alignment.Start
    val bubbleShape = if (isOwn) RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    else RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        if (!isOwn) Text(message.senderName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))

        if (message.messageType.uppercase() == "FILE" && message.files.isNotEmpty()) {
            val file = message.files.first()
            val isImage = file.fileName.isImageFile() || file.contentType.startsWith("image/")
            val isPdf = file.fileName.endsWith(".pdf", ignoreCase = true) || file.contentType == "application/pdf"

            if (isImage) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .clip(bubbleShape)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onFileClick(file) },
                ) {
                    AsyncImage(
                        model = file.url,
                        contentDescription = file.fileName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(240.dp, 200.dp),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(formatChatTime(message.createdAt), style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .clip(bubbleShape)
                        .background(bgColor)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onFileClick(file) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(textColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isPdf) Icons.Filled.PictureAsPdf else Icons.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(file.fileName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = textColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(file.contentType, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.7f))
                    }
                    Icon(Icons.Filled.Download, contentDescription = null, tint = textColor.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
                Text(formatChatTime(message.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp))
            }
        } else {
            Box(modifier = Modifier.widthIn(max = 280.dp).clip(bubbleShape).background(bgColor).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(message.message ?: "", style = MaterialTheme.typography.bodyMedium, color = textColor)
            }
            Text(formatChatTime(message.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp))
        }
        Spacer(Modifier.height(2.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileViewerSheet(file: SupportChatFile, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val isImage = file.fileName.isImageFile() || file.contentType.startsWith("image/")
    val isPdf = file.fileName.endsWith(".pdf", ignoreCase = true) || file.contentType == "application/pdf"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
        containerColor = if (isImage) Color.Black else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.94f).statusBarsPadding(),
    ) {
        if (isImage) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = file.url,
                    contentDescription = file.fileName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Text(
                        text = file.fileName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { uriHandler.openUri(file.url) }) {
                        Icon(Icons.Filled.Download, contentDescription = "Download", tint = Color.White)
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                    Text("Document", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f).padding(start = 4.dp))
                }
                Spacer(Modifier.height(32.dp))
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp))
                            .background(if (isPdf) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isPdf) Icons.Filled.PictureAsPdf else Icons.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = if (isPdf) Color(0xFFD32F2F) else Primary40,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                    Text(file.fileName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                            .background(Primary40)
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                uriHandler.openUri(file.url)
                                onDismiss()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("Open / Download", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun String.isImageFile() =
    endsWith(".jpg", ignoreCase = true) || endsWith(".jpeg", ignoreCase = true) ||
    endsWith(".png", ignoreCase = true) || endsWith(".webp", ignoreCase = true) ||
    endsWith(".gif", ignoreCase = true)

private fun formatChatTime(iso: String): String = try {
    val time = iso.substringAfter("T").substringBefore(".").substringBefore("Z")
    val parts = time.split(":")
    val hour = parts[0].toInt()
    val min = parts.getOrElse(1) { "00" }
    val ampm = if (hour < 12) "AM" else "PM"
    val h = if (hour % 12 == 0) 12 else hour % 12
    "$h:$min $ampm"
} catch (e: Exception) { "" }
