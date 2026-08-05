package dev.juanrincon.simmerly.recipes.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * A duration detected in free-form recipe text, together with the exact substring it was
 * parsed from (so callers can, for instance, underline it in place).
 */
data class ParsedDuration(
    val duration: Duration,
    val sourceText: String
)

private val DURATION_REGEX = Regex(
    """(\d+)\s*(?:[-–—]\s*(\d+)\s*)?(hours?|hrs?|minutes?|mins?|seconds?|secs?)\b""",
    RegexOption.IGNORE_CASE
)

/**
 * Finds every duration mentioned in [text] (typically an [Instruction.text][dev.juanrincon.simmerly.recipes.domain.model.Instruction.text]
 * markdown body), in the order they appear.
 *
 * Ranges such as "8-10 minutes" resolve to the lower bound — the number a cook would actually
 * set a timer to. Bare numeric adjectives ("200 g", "2 eggs") never match, since a unit word is
 * required.
 */
fun parseDurations(text: String): List<ParsedDuration> =
    DURATION_REGEX.findAll(text).mapNotNull { match ->
        val amountText = match.groupValues[1]
        val amount = amountText.toIntOrNull() ?: return@mapNotNull null
        val unit = match.groupValues[3].lowercase()
        val duration = when {
            unit.startsWith("hour") || unit.startsWith("hr") -> amount.hours
            unit.startsWith("minute") || unit.startsWith("min") -> amount.minutes
            else -> amount.seconds
        }
        if (duration <= Duration.ZERO) return@mapNotNull null
        ParsedDuration(duration = duration, sourceText = match.value)
    }.toList()
