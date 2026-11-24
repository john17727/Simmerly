package dev.juanrincon.simmerly.timeline.data.remote

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandler
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandlerNoContent
import dev.juanrincon.simmerly.core.data.remote.dto.ItemListDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeTimelineEventOutDto
import dev.juanrincon.simmerly.timeline.data.remote.dto.MemberDto
import dev.juanrincon.simmerly.timeline.data.remote.dto.TimelineEventUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class TimelineNetworkClient(private val client: HttpClient) {

    /** Same filter Mealie's web UI uses: this recipe, informational/comment/system events. */
    suspend fun getEvents(
        recipeSlug: String,
        page: Int,
        perPage: Int,
    ): Either<DataError.NetworkError<Unit>, ItemListDto<RecipeTimelineEventOutDto>> =
        arrowNetworkHandler {
            client.get("/api/recipes/timeline/events") {
                parameter("page", page)
                parameter("perPage", perPage)
                parameter("orderBy", "timestamp")
                parameter("orderDirection", "desc")
                parameter(
                    "queryFilter",
                    "(recipe.slug=\"$recipeSlug\") AND eventType IN [\"info\", \"system\", \"comment\"]"
                )
            }
        }

    suspend fun updateEvent(
        id: String,
        body: TimelineEventUpdateDto,
    ): Either<DataError.NetworkError<Unit>, RecipeTimelineEventOutDto> = arrowNetworkHandler {
        client.put("/api/recipes/timeline/events/$id") { setBody(body) }
    }

    suspend fun deleteEvent(id: String): Either<DataError.NetworkError<Unit>, Unit> =
        arrowNetworkHandlerNoContent { client.delete("/api/recipes/timeline/events/$id") }

    /** Uploads the photo; the event must already exist. [extension] is without a dot, e.g. `jpg`. */
    suspend fun uploadImage(
        id: String,
        bytes: ByteArray,
        extension: String,
    ): Either<DataError.NetworkError<Unit>, Unit> = arrowNetworkHandlerNoContent {
        client.put("/api/recipes/timeline/events/$id/image") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("extension", extension)
                        append("image", bytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/$extension")
                            append(HttpHeaders.ContentDisposition, "filename=\"timeline.$extension\"")
                        })
                    }
                )
            )
        }
    }

    suspend fun getMembers(): Either<DataError.NetworkError<Unit>, ItemListDto<MemberDto>> =
        arrowNetworkHandler {
            client.get("/api/households/members") {
                parameter("page", 1)
                parameter("perPage", -1)
            }
        }
}
