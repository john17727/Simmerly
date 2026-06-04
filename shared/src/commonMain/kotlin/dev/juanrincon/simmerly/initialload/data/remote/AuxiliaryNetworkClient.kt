package dev.juanrincon.simmerly.initialload.data.remote

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandler
import dev.juanrincon.simmerly.core.data.remote.dto.ItemListDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.CategoryDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.FoodDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.TagDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.ToolDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.UnitDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class AuxiliaryNetworkClient(private val client: HttpClient) {

    suspend fun getTags(): Either<DataError.NetworkError<Unit>, ItemListDto<TagDto>> =
        arrowNetworkHandler {
            client.get("/api/organizers/tags") {
                parameter("perPage", 500)
            }
        }

    suspend fun getCategories(): Either<DataError.NetworkError<Unit>, ItemListDto<CategoryDto>> =
        arrowNetworkHandler {
            client.get("/api/organizers/categories") {
                parameter("perPage", 500)
            }
        }

    suspend fun getTools(): Either<DataError.NetworkError<Unit>, ItemListDto<ToolDto>> =
        arrowNetworkHandler {
            client.get("/api/organizers/tools") {
                parameter("perPage", 500)
            }
        }

    suspend fun getUnits(): Either<DataError.NetworkError<Unit>, ItemListDto<UnitDto>> =
        arrowNetworkHandler {
            client.get("/api/units") {
                parameter("perPage", 500)
            }
        }
}
