package dev.juanrincon.simmerly.timeline.presentation.models

import dev.juanrincon.simmerly.timeline.domain.TimelineEventType

data class TimelineEventUi(
    val id: String,
    val type: TimelineEventType,
    val subject: String,
    val message: String?,
    val date: String,
    /** "Nov 21", shown opposite the card on the desktop zig-zag. */
    val shortDate: String,
    val authorName: String,
    val authorInitials: String,
    val authorImage: String,
    val image: String?,
    val canEdit: Boolean,
)
