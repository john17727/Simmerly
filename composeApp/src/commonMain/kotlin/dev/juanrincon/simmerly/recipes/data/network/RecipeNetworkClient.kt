package dev.juanrincon.simmerly.recipes.data.network

import app.tracktion.core.domain.util.DataError
import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.core.data.network.dto.ItemListDto
import dev.juanrincon.simmerly.core.data.network.networkHandler
import dev.juanrincon.simmerly.recipes.data.network.dto.RecipeDetailDto
import dev.juanrincon.simmerly.recipes.data.network.dto.RecipeSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

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
}