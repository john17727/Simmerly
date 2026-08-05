package dev.juanrincon.simmerly.recipes.presentation.cookmode.models

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

enum class TimerOrigin { DETECTED, USER }

/**
 * A cooking timer, deadline-based rather than tick-based: while running, [deadlineEpochMillis]
 * is a fixed point in wall-clock time, so the remaining duration is always `deadline - now` and
 * needs no correction after backgrounding, a config change, or navigating between steps.
 *
 * Exactly one of [deadlineEpochMillis] / [pausedRemainingMillis] is non-null: running timers
 * carry a deadline, paused ones carry a frozen remaining duration.
 */
data class CookTimerUi(
    val id: String,
    val label: String,
    val total: Duration,
    val deadlineEpochMillis: Long? = null,
    val pausedRemainingMillis: Long? = null,
    val origin: TimerOrigin = TimerOrigin.USER
) {
    val isPaused: Boolean get() = deadlineEpochMillis == null

    fun remaining(nowMillis: Long): Duration = when {
        deadlineEpochMillis != null -> (deadlineEpochMillis - nowMillis).coerceAtLeast(0L).milliseconds
        pausedRemainingMillis != null -> pausedRemainingMillis.milliseconds
        else -> total
    }

    fun isFinished(nowMillis: Long): Boolean = !isPaused && remaining(nowMillis) <= Duration.ZERO

    /** Fraction of [total] elapsed, in `[0f, 1f]`. */
    fun progress(nowMillis: Long): Float {
        if (total <= Duration.ZERO) return 0f
        val elapsed = total - remaining(nowMillis)
        return (elapsed / total).toFloat().coerceIn(0f, 1f)
    }
}
