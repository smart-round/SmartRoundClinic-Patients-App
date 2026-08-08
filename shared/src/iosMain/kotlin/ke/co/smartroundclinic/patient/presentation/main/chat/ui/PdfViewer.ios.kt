package ke.co.smartroundclinic.patient.presentation.main.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument
import platform.PDFKit.PDFView

/** PDFKit fetches and paginates a remote PDF itself, so there is nothing to cache or rasterise. */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PdfViewer(url: String, modifier: Modifier) {
    UIKitView(
        factory = {
            PDFView().apply {
                autoScales = true
                NSURL.URLWithString(url)?.let { document = PDFDocument(uRL = it) }
            }
        },
        modifier = modifier,
    )
}
