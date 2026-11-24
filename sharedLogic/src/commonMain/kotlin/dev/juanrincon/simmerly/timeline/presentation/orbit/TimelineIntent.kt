package dev.juanrincon.simmerly.timeline.presentation.orbit

import dev.juanrincon.simmerly.timeline.domain.TimelineEventType

sealed interface TimelineIntent {
    data object Refresh : TimelineIntent
    data object LoadMore : TimelineIntent
    data class ToggleType(val type: TimelineEventType) : TimelineIntent
    data object ToggleSort : TimelineIntent
    data class DeleteClicked(val eventId: String) : TimelineIntent
    data object DeleteConfirmed : TimelineIntent
    data object DeleteDismissed : TimelineIntent
}
