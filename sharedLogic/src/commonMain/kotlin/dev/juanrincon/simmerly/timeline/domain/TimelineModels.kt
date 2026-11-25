package dev.juanrincon.simmerly.timeline.domain

import app.tracktion.core.domain.util.Error
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class TimelineEventType { Info, Comment, System }

@OptIn(ExperimentalTime::class)
data class TimelineEvent(
    val id: String,
    val recipeId: String,
    val type: TimelineEventType,
    val subject: String,
    val message: String?,
    val timestamp: Instant,
    val authorId: String,
    val authorName: String,
    val authorImage: String,
    /** Full-size photo URL, or null when the entry has none. */
    val image: String?,
    val thumbnail: String?,
    /** True when the current user wrote it or is an admin (Mealie's edit/delete rule). */
    val canEdit: Boolean,
)

/** A photo picked on the device, uploaded after the event is saved. */
class PickedImage(val bytes: ByteArray, val extension: String)

sealed interface TimelineError : Error {
    data object FetchError : TimelineError
    data object UpdateError : TimelineError
    data object DeleteError : TimelineError
}
