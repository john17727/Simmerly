package dev.juanrincon.simmerly.recipes.data.remote

import app.tracktion.core.domain.util.DataError
import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.core.data.remote.dto.ItemListDto
import dev.juanrincon.simmerly.core.data.remote.networkHandler
import dev.juanrincon.simmerly.core.data.remote.storeNetworkHandler
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeDetailDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.mobilenativefoundation.store.store5.FetcherResult

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
    ): FetcherResult<RecipeDetailDto> = storeNetworkHandler<RecipeDetailDto, Unit> {
        client.get("/api/recipes/$slug")
    }
}