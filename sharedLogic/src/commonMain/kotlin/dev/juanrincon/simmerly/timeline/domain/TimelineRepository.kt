package dev.juanrincon.simmerly.timeline.domain

import arrow.core.Either
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import kotlinx.coroutines.flow.Flow

interface TimelineRepository {
    /** Cached events, newest first. Fetches when empty and refetches in the background when stale. */
    fun timeline(recipeId: String): Flow<Either<TimelineError, LoadingResult<List<TimelineEvent>>>>

    /** Explicit refresh (pull-to-refresh): skips the TTL and replaces the cached page(s). */
    suspend fun refresh(recipeId: String): Either<TimelineError, Unit>

    /** Fetches the next page; returns true when more pages remain afterwards. */
    suspend fun loadNextPage(recipeId: String): Either<TimelineError, Boolean>

    suspend fun updateEvent(
        recipeId: String,
        eventId: String,
        subject: String,
        message: String?,
        newImage: PickedImage?,
        removeImage: Boolean,
    ): Either<TimelineError, Unit>

    suspend fun deleteEvent(recipeId: String, eventId: String): Either<TimelineError, Unit>
}
