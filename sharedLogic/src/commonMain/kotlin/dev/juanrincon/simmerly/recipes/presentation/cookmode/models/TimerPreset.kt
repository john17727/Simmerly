package dev.juanrincon.simmerly.recipes.presentation.cookmode.models

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** A duration a cook might want to start a timer for, offered as a tappable preset in the "New
 * timer" sheet — either detected in one of the recipe's steps, or a fixed quick pick. */
data class TimerPreset(
    val duration: Duration,
    val label: String
)

private val QUICK_PICK_MINUTES = listOf(1, 5)

/** Every duration detected anywhere in the recipe, deduplicated, plus a couple of fixed quick
 * picks so the sheet is never empty even for a recipe with no detectable durations. */
fun List<CookStepUi>.toTimerPresets(): List<TimerPreset> {
    val detected = flatMap { step ->
        val firstIngredientLabel = step.ingredients.firstOrNull()?.formattedDisplay
        step.detectedDurations.map { parsed ->
            val durationLabel = formatPresetDuration(parsed.duration)
            TimerPreset(
                duration = parsed.duration,
                label = if (firstIngredientLabel != null) {
                    "$durationLabel · $firstIngredientLabel"
                } else {
                    durationLabel
                }
            )
        }
    }
    val quickPicks = QUICK_PICK_MINUTES.map { minutes ->
        TimerPreset(duration = minutes.minutes, label = formatPresetDuration(minutes.minutes))
    }
    return (detected + quickPicks).distinctBy { it.duration to it.label }
}

private fun formatPresetDuration(duration: Duration): String {
    val minutes = duration.inWholeMinutes
    val seconds = duration.inWholeSeconds % 60
    return when {
        minutes > 0 && seconds == 0L -> "$minutes min"
        minutes > 0 -> "$minutes min $seconds sec"
        else -> "$seconds sec"
    }
}
