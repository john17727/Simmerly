package dev.juanrincon.simmerly.timeline.presentation.orbit

import dev.juanrincon.simmerly.timeline.domain.TimelineEventType
import dev.juanrincon.simmerly.timeline.presentation.models.TimelineEventUi

data class TimelineState(
    val recipeName: String = "",
    /** Pre-formatted date, or null when the recipe was never made. */
    val lastMade: String? = null,
    val events: List<TimelineEventUi> = emptyList(),
    /** Types currently shown; all on by default. */
    val visibleTypes: Set<TimelineEventType> = TimelineEventType.entries.toSet(),
    val newestFirst: Boolean = true,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isPullRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val hasError: Boolean = false,
    /** Event awaiting delete confirmation. */
    val pendingDeleteId: String? = null,
    val isDeleting: Boolean = false,
) {
    val visibleEvents: List<TimelineEventUi>
        get() = events.filter { it.type in visibleTypes }
            .let { if (newestFirst) it else it.reversed() }
}
