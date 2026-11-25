package dev.juanrincon.simmerly.timeline.data

import dev.juanrincon.simmerly.core.data.remote.TimelineImageSize
import dev.juanrincon.simmerly.core.data.remote.timelineImageUrl
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeTimelineEventOutDto
import dev.juanrincon.simmerly.timeline.data.local.TimelineEventEntity
import dev.juanrincon.simmerly.timeline.data.local.TimelineEventWithUser
import dev.juanrincon.simmerly.timeline.domain.TimelineEvent
import dev.juanrincon.simmerly.timeline.domain.TimelineEventType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Mealie's literal for "this entry has a photo"; anything else (or null) means none. */
internal const val IMAGE_PRESENT = "has image"
internal const val IMAGE_ABSENT = "does not have image"

@OptIn(ExperimentalTime::class)
internal fun RecipeTimelineEventOutDto.toEntity(): TimelineEventEntity = TimelineEventEntity(
    id = id,
    recipeId = recipeId,
    userId = userId,
    subject = subject,
    eventType = eventType,
    eventMessage = eventMessage,
    hasImage = image == IMAGE_PRESENT,
    timestamp = Instant.parse(if (timestamp.endsWith("Z") || timestamp.contains('+')) timestamp else "${timestamp}Z"),
)

private fun String.toEventType(): TimelineEventType = when (this) {
    "comment" -> TimelineEventType.Comment
    "system" -> TimelineEventType.System
    else -> TimelineEventType.Info
}

@OptIn(ExperimentalTime::class)
internal fun TimelineEventWithUser.toDomain(
    host: String?,
    currentUserId: String?,
    currentUserIsAdmin: Boolean,
): TimelineEvent = TimelineEvent(
    id = event.id,
    recipeId = event.recipeId,
    type = event.eventType.toEventType(),
    subject = event.subject,
    message = event.eventMessage,
    timestamp = event.timestamp,
    authorId = event.userId,
    authorName = user?.fullName?.takeIf { it.isNotBlank() } ?: user?.username.orEmpty(),
    authorImage = if (host == null) "" else "$host/api/media/users/${event.userId}/profile.webp",
    image = if (event.hasImage) timelineImageUrl(host, event.recipeId, event.id) else null,
    thumbnail = if (event.hasImage) {
        timelineImageUrl(host, event.recipeId, event.id, TimelineImageSize.Thumbnail)
    } else null,
    canEdit = currentUserIsAdmin || (currentUserId != null && currentUserId == event.userId),
)
