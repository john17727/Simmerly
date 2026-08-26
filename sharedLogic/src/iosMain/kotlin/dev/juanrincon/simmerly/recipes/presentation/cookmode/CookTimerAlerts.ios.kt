package dev.juanrincon.simmerly.recipes.presentation.cookmode

import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookTimerUi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

actual fun defaultCookTimerAlerts(): CookTimerAlerts = IosCookTimerAlerts()

/**
 * Schedules a local notification for the moment a timer's deadline passes, so a cook who has
 * backgrounded the app (or locked the phone) while something is on the stove still gets alerted.
 * Foreground finishes are covered separately by haptics in the SwiftUI layer — see
 * `CookModeRoot.swift` — so this deliberately does not need a
 * `UNUserNotificationCenterDelegate` to suppress the banner while active.
 *
 * The request identifier is always the timer's own id, so re-scheduling (e.g. on resume after a
 * pause) replaces any pending request for that timer rather than stacking a second one.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
class IosCookTimerAlerts : CookTimerAlerts {

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    override fun schedule(timer: CookTimerUi) {
        val deadline = timer.deadlineEpochMillis ?: return // paused — nothing to arm
        val nowMillis = Clock.System.now().toEpochMilliseconds()
        val remainingSeconds = (deadline - nowMillis) / 1000.0
        if (remainingSeconds <= 0.0) return // already elapsed — the ticker's own side effect covers this

        val content = UNMutableNotificationContent().apply {
            setTitle("Timer finished")
            setBody(timer.label.ifBlank { "Timer" })
            setSound(UNNotificationSound.defaultSound)
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = remainingSeconds,
            repeats = false
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = timer.id,
            content = content,
            trigger = trigger
        )
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override fun cancel(timerId: String) {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(timerId))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(timerId))
    }
}
