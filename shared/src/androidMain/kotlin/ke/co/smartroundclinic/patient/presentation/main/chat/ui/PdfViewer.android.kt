package ke.co.smartroundclinic.patient.presentation.main.chat.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

/**
 * PdfRenderer needs a seekable file descriptor, so the document is cached to app-private
 * storage first and its pages are rasterised on the IO dispatcher.
 */
@Composable
actual fun PdfViewer(url: String, modifier: Modifier) {
    val context = LocalContext.current
    var pages by remember(url) { mutableStateOf<List<ImageBitmap>>(emptyList()) }
    var error by remember(url) { mutableStateOf<String?>(null) }
    var isLoading by remember(url) { mutableStateOf(true) }

    LaunchedEffect(url) {
        isLoading = true
        error = null
        runCatching {
            withContext(Dispatchers.IO) {
                val cached = File(context.cacheDir, "pdf_${url.hashCode()}.pdf")
                if (!cached.exists() || cached.length() == 0L) {
                    URL(url).openStream().use { input ->
                        cached.outputStream().use { output -> input.copyTo(output) }
                    }
                }
                ParcelFileDescriptor.open(cached, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                    PdfRenderer(fd).use { renderer ->
                        (0 until renderer.pageCount).map { index ->
                            renderer.openPage(index).use { page ->
                                // 2x so text stays sharp once the page is scaled to screen width.
                                val bitmap = Bitmap.createBitmap(
                                    page.width * 2,
                                    page.height * 2,
                                    Bitmap.Config.ARGB_8888,
                                )
                                bitmap.eraseColor(android.graphics.Color.WHITE)
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                bitmap.asImageBitmap()
                            }
                        }
                    }
                }
            }
        }.onSuccess { pages = it }
            .onFailure { error = "Couldn't open this document" }
        isLoading = false
    }

    Box(modifier = modifier.background(Color(0xFF3A3A3A)), contentAlignment = Alignment.Center) {
        when {
            isLoading -> CircularProgressIndicator(color = Color.White)
            error != null -> Text(
                text = error ?: "",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
            )
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(pages) { page ->
                    Image(
                        bitmap = page,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}
