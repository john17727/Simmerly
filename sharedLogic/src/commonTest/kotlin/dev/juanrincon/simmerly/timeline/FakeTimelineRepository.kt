package dev.juanrincon.simmerly.timeline

import arrow.core.Either
import arrow.core.right
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.timeline.domain.PickedImage
import dev.juanrincon.simmerly.timeline.domain.TimelineError
import dev.juanrincon.simmerly.timeline.domain.TimelineEvent
import dev.juanrincon.simmerly.timeline.domain.TimelineEventType
import dev.juanrincon.simmerly.timeline.domain.TimelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun aTimelineEvent(
    id: String = "e1",
    type: TimelineEventType = TimelineEventType.Comment,
    subject: String = "Juan made this",
    message: String? = "Tasty",
    timestamp: Instant = Instant.parse("2026-11-21T10:00:00Z"),
    authorName: String = "Juan Rincon",
    image: String? = null,
    canEdit: Boolean = true,
) = TimelineEvent(
    id = id,
    recipeId = "recipe-1",
    type = type,
    subject = subject,
    message = message,
    timestamp = timestamp,
    authorId = "user-1",
    authorName = authorName,
    authorImage = "",
    image = image,
    thumbnail = image,
    canEdit = canEdit,
)

class FakeTimelineRepository : TimelineRepository {
    val timelineFlow = MutableSharedFlow<Either<TimelineError, LoadingResult<List<TimelineEvent>>>>(replay = 1)

    var refreshResult: Either<TimelineError, Unit> = Unit.right()
    var loadNextPageResult: Either<TimelineError, Boolean> = false.right()
    var updateResult: Either<TimelineError, Unit> = Unit.right()
    var deleteResult: Either<TimelineError, Unit> = Unit.right()

    var refreshCount = 0
    var loadNextPageCount = 0
    var lastUpdate: UpdateCall? = null
    val deletedIds = mutableListOf<String>()

    data class UpdateCall(
        val eventId: String,
        val subject: String,
        val message: String?,
        val newImage: PickedImage?,
        val removeImage: Boolean,
    )

    override fun timeline(recipeId: String): Flow<Either<TimelineError, LoadingResult<List<TimelineEvent>>>> =
        timelineFlow

    override suspend fun refresh(recipeId: String): Either<TimelineError, Unit> {
        refreshCount++
        return refreshResult
    }

    override suspend fun loadNextPage(recipeId: String): Either<TimelineError, Boolean> {
        loadNextPageCount++
        return loadNextPageResult
    }

    override suspend fun updateEvent(
        recipeId: String,
        eventId: String,
        subject: String,
        message: String?,
        newImage: PickedImage?,
        removeImage: Boolean,
    ): Either<TimelineError, Unit> {
        lastUpdate = UpdateCall(eventId, subject, message, newImage, removeImage)
        return updateResult
    }

    override suspend fun deleteEvent(recipeId: String, eventId: String): Either<TimelineError, Unit> {
        deletedIds += eventId
        return deleteResult
    }
}
