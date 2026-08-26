package dev.juanrincon.simmerly.recipes.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * A duration detected in free-form recipe text, together with the exact substring it was
 * parsed from (so callers can, for instance, underline it in place).
 *
 * [duration] is always the value a cook would start from — for a range like "15–17 minutes" that
 * is the lower bound, with [upperBound] carrying the top. [upperBound] is null when the text gave
 * a single duration, so callers that only ever want one number can keep reading [duration].
 */
data class ParsedDuration(
    val duration: Duration,
    val sourceText: String,
    val upperBound: Duration? = null
) {
    /** True when the source text gave a span ("15–17 minutes") rather than a single value. */
    val isRange: Boolean get() = upperBound != null && upperBound > duration

    /** [duration] in milliseconds. `Duration` bridges to Swift as an opaque raw Long (Kotlin's
     * internal encoding, not a millisecond count), so Swift can't do arithmetic on [duration]
     * directly — this is the number it actually wants. */
    val durationMillis: Long get() = duration.inWholeMilliseconds

    /** [upperBound] in milliseconds — see [durationMillis] for why this exists. */
    val upperBoundMillis: Long? get() = upperBound?.inWholeMilliseconds
}

private val DURATION_REGEX = Regex(
    """(\d+)\s*(?:(?:[-–—]|\bto\b)\s*(\d+)\s*)?(hours?|hrs?|minutes?|mins?|seconds?|secs?)\b""",
    RegexOption.IGNORE_CASE
)

/**
 * Finds every duration mentioned in [text] (typically an [Instruction.text][dev.juanrincon.simmerly.recipes.domain.model.Instruction.text]
 * markdown body), in the order they appear.
 *
 * Ranges such as "8-10 minutes" or "8 to 10 minutes" keep both bounds — see [ParsedDuration] and
 * [suggestedTimerOptions]. Bare numeric adjectives ("200 g", "2 eggs") never match, since a unit
 * word is required.
 */
fun parseDurations(text: String): List<ParsedDuration> =
    DURATION_REGEX.findAll(text).mapNotNull { match ->
        val unit = match.groupValues[3].lowercase()
        val low = match.groupValues[1].toIntOrNull()?.toDuration(unit) ?: return@mapNotNull null
        if (low <= Duration.ZERO) return@mapNotNull null
        val high = match.groupValues[2].toIntOrNull()?.toDuration(unit)
        ParsedDuration(
            duration = low,
            sourceText = match.value,
            upperBound = high?.takeIf { it > low }
        )
    }.toList()

private fun Int.toDuration(unit: String): Duration = when {
    unit.startsWith("hour") || unit.startsWith("hr") -> hours
    unit.startsWith("minute") || unit.startsWith("min") -> minutes
    else -> seconds
}

/**
 * The most timer options a range should ever offer. A cook glancing at a phone with wet hands can
 * pick from a handful of chips; twenty is a wall of numbers, not a choice.
 */
private const val MAX_RANGE_OPTIONS = 6

/**
 * Increments to coarsen a wide range by, smallest first. Every entry is a step a cook would
 * actually think in — nobody sets a 7-minute-interval timer.
 */
private val RANGE_STEP_LADDER = listOf(1L, 2L, 5L, 10L, 15L, 30L, 60L)

/**
 * The timer durations worth offering for this detection.
 *
 * A single duration yields exactly itself. A range yields several choices spanning it, always
 * including both bounds so the cook can commit to the short or the long end. Narrow ranges list
 * every whole unit ("15–17 minutes" → 15, 16, 17, matching the design); wider ones coarsen to a
 * round step rather than dumping one chip per minute ("45–60 minutes" → 45, 50, 55, 60, and
 * "1–2 hours" → 60, 75, 90, 105, 120).
 *
 * Units follow the range's own scale: a sub-minute lower bound counts in seconds, anything else
 * in minutes.
 */
fun ParsedDuration.suggestedTimerOptions(maxOptions: Int = MAX_RANGE_OPTIONS): List<Duration> {
    val high = upperBound
    if (high == null || high <= duration) return listOf(duration)

    // "45 seconds to 2 minutes" should step in seconds, not round the lower bound down to 0.
    val inSeconds = duration.inWholeMinutes == 0L
    val low = if (inSeconds) duration.inWholeSeconds else duration.inWholeMinutes
    val top = if (inSeconds) high.inWholeSeconds else high.inWholeMinutes
    if (top <= low) return listOf(duration)

    val span = top - low
    val step = RANGE_STEP_LADDER.firstOrNull { span / it + 1 <= maxOptions }
        ?: (span / (maxOptions - 1).coerceAtLeast(1)).coerceAtLeast(1L)

    return buildList {
        var value = low
        while (value < top) {
            add(value)
            value += step
        }
        // The top of the range is always worth offering, even when the step overshot it.
        add(top)
    }.map { if (inSeconds) it.seconds else it.minutes }
}
