package dev.juanrincon.simmerly.recipes.presentation.cookmode

import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookTimerUi

/**
 * Seam for alerting the cook when a timer finishes while Cook Mode isn't in the foreground.
 *
 * Today this is a no-op — timers are purely deadline-based in-memory state, so nothing fires
 * once the app is backgrounded or killed. A real implementation (Android foreground service +
 * notification channel, say) can be dropped in behind this interface without [CookModeViewModel]
 * or any Cook Mode composable knowing alerting exists.
 */
interface CookTimerAlerts {
    fun schedule(timer: CookTimerUi)
    fun cancel(timerId: String)
}

object NoOpCookTimerAlerts : CookTimerAlerts {
    override fun schedule(timer: CookTimerUi) = Unit
    override fun cancel(timerId: String) = Unit
}

/** The [CookTimerAlerts] a platform actually wants wired up by default. Android and desktop stay
 * on [NoOpCookTimerAlerts] — this seam was designed but never implemented there. iOS provides a
 * real `UNUserNotificationCenter`-backed implementation, since that's this port's whole point. */
expect fun defaultCookTimerAlerts(): CookTimerAlerts
