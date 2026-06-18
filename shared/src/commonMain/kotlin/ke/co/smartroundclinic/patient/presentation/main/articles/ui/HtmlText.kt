package ke.co.smartroundclinic.patient.presentation.main.articles.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import ke.co.smartroundclinic.patient.presentation.theme.Primary40

@Composable
internal fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
) {
    val annotated = remember(html) { html.toAnnotatedString() }
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
}

private data class HtmlOpenTag(val name: String, val start: Int, val extra: String)

private fun String.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    val src = this@toAnnotatedString
    val stack = mutableListOf<HtmlOpenTag>()
    val listTypes = mutableListOf<String>()
    val listCounters = mutableListOf<Int>()
    var lastChar: Char? = null

    fun emit(s: String) { if (s.isNotEmpty()) { append(s); lastChar = s.last() } }
    fun nl() { if (lastChar != '\n') emit("\n") }
    fun nl2() { if (lastChar == null) return; if (lastChar == '\n') emit("\n") else emit("\n\n") }

    fun closeStyle(names: Set<String>, style: () -> SpanStyle) {
        val idx = stack.indexOfLast { it.name in names }
        if (idx >= 0) { val e = stack.removeAt(idx); addStyle(style(), e.start, length) }
    }

    var i = 0
    while (i < src.length) {
        if (src[i] != '<') {
            val end = src.indexOf('<', i).takeIf { it >= 0 } ?: src.length
            emit(src.substring(i, end).decodeHtmlEntities())
            i = end
            continue
        }
        val closeAngle = src.indexOf('>', i)
        if (closeAngle < 0) { emit(src.substring(i)); break }
        val inner = src.substring(i + 1, closeAngle).trim()
        i = closeAngle + 1
        if (inner.isEmpty()) continue

        val isClose = inner.startsWith('/')
        val body = if (isClose) inner.drop(1).trimStart() else inner
        val spaceIdx = body.indexOfFirst { it == ' ' || it == '\t' }
        val name = (if (spaceIdx >= 0) body.take(spaceIdx) else body).lowercase().trimEnd('/')
        val attrStr = if (spaceIdx >= 0) body.drop(spaceIdx) else ""

        if (name == "br") { emit("\n"); continue }
        if (body.endsWith('/') && !isClose) continue

        if (isClose) {
            when (name) {
                "b", "strong" -> closeStyle(setOf("b", "strong")) { SpanStyle(fontWeight = FontWeight.Bold) }
                "i", "em" -> closeStyle(setOf("i", "em")) { SpanStyle(fontStyle = FontStyle.Italic) }
                "u" -> closeStyle(setOf("u")) { SpanStyle(textDecoration = TextDecoration.Underline) }
                "s", "del", "strike" -> closeStyle(setOf("s", "del", "strike")) { SpanStyle(textDecoration = TextDecoration.LineThrough) }
                "h1", "h2", "h3", "h4" -> {
                    val idx = stack.indexOfLast { it.name == name }
                    if (idx >= 0) {
                        val e = stack.removeAt(idx)
                        val (sz, wt) = when (name) {
                            "h1" -> 26.sp to FontWeight.Bold
                            "h2" -> 22.sp to FontWeight.Bold
                            "h3" -> 19.sp to FontWeight.SemiBold
                            else -> 17.sp to FontWeight.SemiBold
                        }
                        addStyle(SpanStyle(fontSize = sz, fontWeight = wt), e.start, length)
                    }
                    nl()
                }
                "a" -> {
                    val idx = stack.indexOfLast { it.name == "a" }
                    if (idx >= 0) {
                        val e = stack.removeAt(idx)
                        if (e.extra.isNotEmpty()) {
                            addLink(
                                LinkAnnotation.Url(
                                    e.extra,
                                    TextLinkStyles(SpanStyle(color = Primary40, textDecoration = TextDecoration.Underline)),
                                ),
                                e.start, length,
                            )
                        }
                    }
                }
                "p" -> nl2()
                "ul", "ol" -> { listTypes.removeLastOrNull(); listCounters.removeLastOrNull(); nl() }
                "li" -> {}
            }
        } else {
            when (name) {
                "b", "strong", "i", "em", "u", "s", "del", "strike" -> stack.add(HtmlOpenTag(name, length, ""))
                "h1", "h2", "h3", "h4" -> { nl(); stack.add(HtmlOpenTag(name, length, "")) }
                "a" -> {
                    val href = Regex("""href=["']([^"']*)["']""").find(attrStr)?.groupValues?.getOrNull(1) ?: ""
                    stack.add(HtmlOpenTag("a", length, href))
                }
                "p" -> { if (length > 0) nl() }
                "ul" -> { listTypes.add("ul"); listCounters.add(0) }
                "ol" -> { listTypes.add("ol"); listCounters.add(0) }
                "li" -> {
                    val prefix = if (listTypes.lastOrNull() == "ol") {
                        val n = (listCounters.lastOrNull() ?: 0) + 1
                        if (listCounters.isNotEmpty()) listCounters[listCounters.size - 1] = n
                        "$n. "
                    } else "• "
                    nl()
                    emit(prefix)
                }
            }
        }
    }
}

private fun String.decodeHtmlEntities() = replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&apos;", "'")
    .replace("&nbsp;", " ")
    .replace("&#39;", "'")
    .replace("&period;", ".")
    .replace("&comma;", ",")
    .replace("&colon;", ":")
    .replace("&semi;", ";")
    .replace("&excl;", "!")
    .replace("&quest;", "?")
    .replace(Regex("&#(\\d+);")) { it.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: it.value }
    .replace(Regex("&#x([0-9a-fA-F]+);")) { it.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: it.value }
