package dev.juanrincon.simmerly.shoppinglists.data.remote

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import arrow.core.raise.either
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandler
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandlerNoContent
import dev.juanrincon.simmerly.core.data.remote.dto.ItemListDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.ParsedIngredientDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.ShoppingFoodDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.ShoppingListDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.ShoppingListItemsCollectionDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.ShoppingUnitDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.IngredientParseRequestDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.NewShoppingListDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.NewShoppingListItemDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.RecipeDecrementDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.RecipeIncrementDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.RecipeIngredientDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.ShoppingListItemUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Mealie's household shopping list API (`/api/households/shopping/…`), plus the unit and food
 * lookups the item editor needs. */
class ShoppingListNetworkClient(private val client: HttpClient) {

    /** A `perPage` of -1 asks Mealie for every list in one page. */
    suspend fun getLists(
        page: Int,
        perPage: Int,
        orderBy: String,
        orderDirection: String,
    ): Either<DataError.NetworkError<Unit>, ItemListDto<ShoppingListDto>> = arrowNetworkHandler {
        client.get("/api/households/shopping/lists") {
            parameter("page", page)
            parameter("perPage", perPage)
            parameter("orderBy", orderBy)
            parameter("orderDirection", orderDirection)
        }
    }

    suspend fun getList(id: String): Either<DataError.NetworkError<Unit>, ShoppingListDto> =
        arrowNetworkHandler { client.get("/api/households/shopping/lists/$id") }

    suspend fun createList(name: String): Either<DataError.NetworkError<Unit>, ShoppingListDto> =
        arrowNetworkHandler {
            client.post("/api/households/shopping/lists") { setBody(NewShoppingListDto(name)) }
        }

    /**
     * `PUT /lists/{id}` replaces the whole list, items included, so the list is re-read as raw
     * JSON and sent back with only its name changed. Round-tripping through [ShoppingListDto]
     * instead would drop the fields it doesn't model and risk the server clearing them.
     */
    suspend fun renameList(
        id: String,
        name: String
    ): Either<DataError.NetworkError<Unit>, ShoppingListDto> = either {
        val current: JsonObject =
            arrowNetworkHandler<Unit, JsonObject> { client.get("/api/households/shopping/lists/$id") }
                .bind()
        val renamed = JsonObject(current + ("name" to JsonPrimitive(name)))
        arrowNetworkHandler<Unit, ShoppingListDto> {
            client.put("/api/households/shopping/lists/$id") { setBody(renamed) }
        }.bind()
    }

    /** Returns the deleted list, which nothing needs. */
    suspend fun deleteList(id: String): Either<DataError.NetworkError<Unit>, Unit> =
        arrowNetworkHandlerNoContent { client.delete("/api/households/shopping/lists/$id") }

    /** Adds a recipe's ingredients once more (or the first time), or just [ingredients] of them. */
    suspend fun addRecipe(
        listId: String,
        recipeId: String,
        quantity: Double,
        ingredients: List<RecipeIngredientDto>? = null,
    ): Either<DataError.NetworkError<Unit>, ShoppingListDto> = arrowNetworkHandler {
        client.post("/api/households/shopping/lists/$listId/recipe/$recipeId") {
            setBody(RecipeIncrementDto(quantity, ingredients))
        }
    }

    /** Takes one serving's worth of a recipe's ingredients off; at zero the recipe is unlinked. */
    suspend fun removeRecipe(
        listId: String,
        recipeId: String,
        quantity: Double,
    ): Either<DataError.NetworkError<Unit>, ShoppingListDto> = arrowNetworkHandler {
        client.post("/api/households/shopping/lists/$listId/recipe/$recipeId/delete") {
            setBody(RecipeDecrementDto(quantity))
        }
    }

    suspend fun createItem(
        item: NewShoppingListItemDto
    ): Either<DataError.NetworkError<Unit>, ShoppingListItemsCollectionDto> = arrowNetworkHandler {
        client.post("/api/households/shopping/items") { setBody(item) }
    }

    suspend fun updateItem(
        item: ShoppingListItemUpdateDto
    ): Either<DataError.NetworkError<Unit>, ShoppingListItemsCollectionDto> = arrowNetworkHandler {
        client.put("/api/households/shopping/items/${item.id}") { setBody(item) }
    }

    suspend fun updateItems(
        items: List<ShoppingListItemUpdateDto>
    ): Either<DataError.NetworkError<Unit>, ShoppingListItemsCollectionDto> = arrowNetworkHandler {
        client.put("/api/households/shopping/items") { setBody(items) }
    }

    /** Sends each id as its own `ids` query parameter, which is how the endpoint takes a list. */
    suspend fun deleteItems(ids: List<String>): Either<DataError.NetworkError<Unit>, Unit> =
        arrowNetworkHandlerNoContent {
            client.delete("/api/households/shopping/items") {
                ids.forEach { parameter("ids", it) }
            }
        }

    /** A `perPage` of -1 asks Mealie for every unit in one page. */
    suspend fun getUnits(
        page: Int,
        perPage: Int,
        orderBy: String,
        orderDirection: String,
    ): Either<DataError.NetworkError<Unit>, ItemListDto<ShoppingUnitDto>> = arrowNetworkHandler {
        client.get("/api/units") {
            parameter("page", page)
            parameter("perPage", perPage)
            parameter("orderBy", orderBy)
            parameter("orderDirection", orderDirection)
        }
    }

    suspend fun searchFoods(
        query: String,
        page: Int,
        perPage: Int,
    ): Either<DataError.NetworkError<Unit>, ItemListDto<ShoppingFoodDto>> = arrowNetworkHandler {
        client.get("/api/foods") {
            parameter("search", query)
            parameter("page", page)
            parameter("perPage", perPage)
        }
    }

    suspend fun parseIngredient(
        text: String
    ): Either<DataError.NetworkError<Unit>, ParsedIngredientDto> = arrowNetworkHandler {
        client.post("/api/parser/ingredient") { setBody(IngredientParseRequestDto(text)) }
    }
}
