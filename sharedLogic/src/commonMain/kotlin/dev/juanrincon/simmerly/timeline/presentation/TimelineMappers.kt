package dev.juanrincon.simmerly.timeline.presentation

import dev.juanrincon.simmerly.timeline.domain.TimelineEvent
import dev.juanrincon.simmerly.timeline.presentation.models.TimelineEventUi
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun TimelineEvent.toUi(): TimelineEventUi = TimelineEventUi(
    id = id,
    type = type,
    subject = subject,
    message = message?.takeIf { it.isNotBlank() },
    date = formatTimelineDate(timestamp),
    authorName = authorName,
    authorInitials = authorName.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() },
    authorImage = authorImage,
    image = image,
    canEdit = canEdit,
)
