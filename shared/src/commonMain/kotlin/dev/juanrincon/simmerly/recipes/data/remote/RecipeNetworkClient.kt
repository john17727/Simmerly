package dev.juanrincon.simmerly.recipes.data.remote

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandler
import dev.juanrincon.simmerly.core.data.remote.dto.ItemListDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.CommentDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeDetailDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeSummaryDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing.NewCommentDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing.RecipePatchDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class RecipeNetworkClient(private val client: HttpClient) {
    suspend fun getRecipes(
        page: Int = 1,
        perPage: Int = 50,
        requireTags: Boolean = false
    ): Either<DataError.NetworkError<Unit>, ItemListDto<RecipeSummaryDto>> = arrowNetworkHandler {
        client.get("/api/recipes") {
            parameter("page", page)
            parameter("perPage", perPage)
            parameter("requireAllTags", requireTags)
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
}