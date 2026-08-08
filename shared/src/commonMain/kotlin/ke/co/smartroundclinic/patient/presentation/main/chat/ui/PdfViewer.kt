package ke.co.smartroundclinic.patient.presentation.main.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders a PDF in-app, so a patient never has to hand a medical document to another app.
 *
 * Deliberately backed by each platform's own renderer — Android's PdfRenderer and iOS PDFKit —
 * rather than a pdfium-based library. Every KMP PDF library bundles pdfium for each ABI, which
 * costs roughly 20MB; the platform renderers are already on the device and add nothing.
 */
@Composable
expect fun PdfViewer(url: String, modifier: Modifier)
