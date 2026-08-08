package ke.co.smartroundclinic.patient.presentation.main.chat.util

/**
 * How a chat attachment should be presented.
 *
 * Raw filenames make poor labels: a camera capture is named with a generated id and a video
 * from the gallery with something like "VID-20260629-WA0017.mp4", neither of which tells the
 * reader anything. Media gets a generic label instead; documents keep their name, because with
 * a document the name is the only thing distinguishing one from another.
 */
enum class AttachmentKind { PHOTO, VIDEO, DOCUMENT, OTHER }

private val PHOTO_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "heic", "heif", "bmp")
private val VIDEO_EXTENSIONS = setOf("mp4", "mov", "avi", "mkv", "webm", "3gp", "m4v", "mpeg", "mpg")
private val DOCUMENT_EXTENSIONS = setOf("pdf", "doc", "docx", "xls", "xlsx", "csv", "ppt", "pptx", "txt", "rtf")

/** contentType is authoritative; the extension is the fallback for records without a reliable one. */
fun attachmentKind(fileName: String, contentType: String): AttachmentKind {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when {
        contentType.startsWith("image/", ignoreCase = true) || ext in PHOTO_EXTENSIONS -> AttachmentKind.PHOTO
        contentType.startsWith("video/", ignoreCase = true) || ext in VIDEO_EXTENSIONS -> AttachmentKind.VIDEO
        ext in DOCUMENT_EXTENSIONS -> AttachmentKind.DOCUMENT
        contentType == "application/pdf" -> AttachmentKind.DOCUMENT
        else -> AttachmentKind.OTHER
    }
}

/** Label for the attachment bubble. Documents keep their filename; everything else is generic. */
fun attachmentLabel(fileName: String, contentType: String): String =
    when (attachmentKind(fileName, contentType)) {
        AttachmentKind.PHOTO -> "Photo"
        AttachmentKind.VIDEO -> "Video"
        AttachmentKind.DOCUMENT -> fileName
        AttachmentKind.OTHER -> "File"
    }

fun isPdf(fileName: String, contentType: String): Boolean =
    contentType == "application/pdf" || fileName.endsWith(".pdf", ignoreCase = true)
