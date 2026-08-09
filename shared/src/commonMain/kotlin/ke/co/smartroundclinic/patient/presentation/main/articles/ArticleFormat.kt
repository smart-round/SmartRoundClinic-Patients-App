package ke.co.smartroundclinic.patient.presentation.main.articles

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Rough reading time shown on cards and the detail byline — the design always reads "N min read". */
internal fun readMinutes(contentHtml: String): Int {
    val words = contentHtml
        .replace(Regex("<[^>]+>"), " ")
        .trim()
        .split(Regex("\\s+"))
        .count { it.isNotBlank() }
    return maxOf(1, words / 200)
}

/** "20 Nov" — the short form the cards use. */
internal fun formatDayMonth(isoDate: String): String = formatDate(isoDate) { day, month, _ ->
    "$day $month"
}

/** "20 Nov, 2025" — the long form the detail byline uses. */
internal fun formatLongDate(isoDate: String): String = formatDate(isoDate) { day, month, year ->
    "$day $month, $year"
}

private inline fun formatDate(isoDate: String, build: (Int, String, Int) -> String): String = try {
    val dateTime = Instant.parse(isoDate).toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    build(dateTime.dayOfMonth, month, dateTime.year)
} catch (_: Exception) {
    ""
}
