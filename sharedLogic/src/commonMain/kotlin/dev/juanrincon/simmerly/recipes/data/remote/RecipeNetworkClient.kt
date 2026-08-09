package dev.juanrincon.simmerly.recipes.data.remote

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandler
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandlerNoContent
import dev.juanrincon.simmerly.core.data.remote.dto.ItemListDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.CommentDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeDetailDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeSummaryDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeTimelineEventOutDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing.NewCommentDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing.RecipeLastMadeDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing.RecipePatchDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing.RecipeTimelineEventInDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing.UserRatingUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlin.time.Instant

class RecipeNetworkClient(private val client: HttpClient) {
    suspend fun getRecipes(
        next: String?,
        requireTags: Boolean = false
    ): Either<DataError.NetworkError<Unit>, ItemListDto<RecipeSummaryDto>> = arrowNetworkHandler {
        if (next != null) {
           client.get("api/$next") {
               parameter("requireAllTags", requireTags)
           }
        } else {
            client.get("/api/recipes") {
                parameter("page", 1)
                parameter("perPage", 50)
                parameter("requireAllTags", requireTags)
            }
        }
    }

    suspend fun getRecipe(
        slug: String
    ): Either<DataError.NetworkError<Unit>, RecipeDetailDto> = arrowNetworkHandler {
        client.get("/api/recipes/$slug")
    }

    suspend fun patchRecipe(
        slug: String,
        recipe: RecipePatchDto
    ): Either<DataError.NetworkError<Unit>, RecipeDetailDto> = arrowNetworkHandler {
        client.patch("/api/recipes/$slug") {
            setBody(recipe)
        }
    }

    suspend fun addComment(
        recipeId: String,
        comment: String
    ): Either<DataError.NetworkError<Unit>, CommentDto> = arrowNetworkHandler {
        client.post("/api/comments") {
            setBody(NewCommentDto(recipeId, comment))
        }
    }

    /** The spec declares this endpoint's success response as an empty body — unverified against a
     * live instance (see the Cook Mode ratings plan's Known Risks). If that turns out wrong,
     * swap [arrowNetworkHandlerNoContent] for [arrowNetworkHandler] with the real response DTO. */
    suspend fun setRating(
        userId: String,
        slug: String,
        rating: Double?
    ): Either<DataError.NetworkError<Unit>, Unit> = arrowNetworkHandlerNoContent {
        client.post("/api/users/$userId/ratings/$slug") {
            setBody(UserRatingUpdateDto(rating))
        }
    }

    /** Despite the spec declaring an empty response, this endpoint actually returns the full
     * recipe object — confirmed against a live instance — so it's decoded exactly like
     * [patchRecipe]/[getRecipe] rather than through the no-content path. */
    suspend fun updateLastMade(
        slug: String,
        timestamp: Instant
    ): Either<DataError.NetworkError<Unit>, RecipeDetailDto> = arrowNetworkHandler {
        client.patch("/api/recipes/$slug/last-made") {
            setBody(RecipeLastMadeDto(timestamp.toString()))
        }
    }

    /**
     * Confirmed against a live instance to return the full [RecipeTimelineEventOutDto], matching
     * its declared `201` schema.
     *
     * Mirrors the exact body Mealie's own UI sends — `recipeId`, `subject`, `eventType`,
     * `eventMessage`, `timestamp` — deliberately omitting `userId` (the server attributes the
     * event from the auth token) and `image` (the server defaults it).
     */
    suspend fun createTimelineEvent(
        recipeId: String,
        subject: String,
        eventMessage: String?,
        timestamp: Instant
    ): Either<DataError.NetworkError<Unit>, RecipeTimelineEventOutDto> = arrowNetworkHandler {
        client.post("/api/recipes/timeline/events") {
            setBody(
                RecipeTimelineEventInDto(
                    recipeId = recipeId,
                    subject = subject,
                    eventType = "comment",
                    eventMessage = eventMessage,
                    timestamp = timestamp.toString()
                )
            )
        }
    }
}