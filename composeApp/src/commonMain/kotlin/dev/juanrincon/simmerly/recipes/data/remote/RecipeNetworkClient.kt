package dev.juanrincon.simmerly.recipes.data.remote

import app.tracktion.core.domain.util.DataError
import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.core.data.remote.dto.ItemListDto
import dev.juanrincon.simmerly.core.data.remote.networkHandler
import dev.juanrincon.simmerly.recipes.data.remote.dto.CommentDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeDetailDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeSummaryDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing.NewCommentDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class RecipeNetworkClient(private val client: HttpClient) {
    suspend fun getRecipes(
        page: Int = 1,
        perPage: Int = 50
    ): Result<ItemListDto<RecipeSummaryDto>, DataError.NetworkError<Unit>> = networkHandler {
        client.get("/api/recipes") {
            parameter("page", page)
            parameter("perPage", perPage)
        }
    }

    suspend fun getRecipe(
        slug: String
    ): Result<RecipeDetailDto, DataError.NetworkError<Unit>> = networkHandler {
        client.get("/api/recipes/$slug")
    }

//    suspend fun updateRecipe()

    suspend fun addComment(recipeId: String, comment: String): Result<CommentDto, DataError.NetworkError<Unit>> = networkHandler {
       client.post("/api/comments") {
           setBody(NewCommentDto(recipeId, comment))
       }
    }
}