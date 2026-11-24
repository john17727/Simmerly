package dev.juanrincon.simmerly.timeline.presentation

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** "Sun, Nov 22, 2026", as drawn in the designs. */
@OptIn(ExperimentalTime::class)
fun formatTimelineDate(instant: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val d = instant.toLocalDateTime(zone)
    return "${dayNames[d.dayOfWeek.ordinal]}, ${monthNames[d.month.ordinal]} ${d.day}, ${d.year}"
}

/** "Nov 21" for the desktop timeline's date column. */
@OptIn(ExperimentalTime::class)
fun formatMonthDay(instant: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val d = instant.toLocalDateTime(zone)
    return "${monthNames[d.month.ordinal]} ${d.day}"
}

/** "Nov 22, 2026" for the "Last made" card. */
@OptIn(ExperimentalTime::class)
fun formatShortDate(instant: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val d = instant.toLocalDateTime(zone)
    return "${monthNames[d.month.ordinal]} ${d.day}, ${d.year}"
}
