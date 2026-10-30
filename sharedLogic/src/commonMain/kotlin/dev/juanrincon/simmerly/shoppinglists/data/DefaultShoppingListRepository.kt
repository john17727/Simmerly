package dev.juanrincon.simmerly.shoppinglists.data

import arrow.core.Either
import arrow.core.raise.either
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.shoppinglists.data.mappers.toDomain
import dev.juanrincon.simmerly.shoppinglists.data.mappers.toDto
import dev.juanrincon.simmerly.shoppinglists.data.mappers.toUpdateDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.ShoppingListNetworkClient
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.ShoppingListItemsCollectionDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.NewShoppingListItemDto
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListError
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.domain.model.ParsedIngredient
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingFood
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingListItem
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingUnit
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Every list or unit fits in one page when Mealie is asked for `perPage = -1`. */
private const val ALL_ITEMS = -1

/** How many foods the add-item and editor searches show. */
private const val FOOD_SEARCH_PAGE_SIZE = 50

class DefaultShoppingListRepository(
    private val networkClient: ShoppingListNetworkClient,
    private val sessionDataStore: SessionDataStore,
) : ShoppingListRepository {

    /** `null` until the first successful [refreshLists]; the detail screen can still populate a
     * single entry through [refreshList] before that. */
    private val lists = MutableStateFlow<List<ShoppingList>?>(null)

    /** Units barely change during a session, so the first load is reused. */
    private var cachedUnits: List<ShoppingUnit>? = null

    override fun observeLists(): Flow<List<ShoppingList>?> = lists

    override fun observeList(id: String): Flow<ShoppingList?> =
        lists.map { all -> all?.firstOrNull { it.id == id } }.distinctUntilChanged()

    override suspend fun refreshLists(): Either<ShoppingListError, Unit> = either {
        val host = sessionDataStore.getServerAddress()
        val summaries = networkClient.getLists(page = 1, perPage = ALL_ITEMS, orderBy = "name", orderDirection = "asc")
            .mapLeft { ShoppingListError.FetchError }.bind()
        val detailed = coroutineScope {
            summaries.items.map { summary -> async { networkClient.getList(summary.id) } }
                .awaitAll()
        }.map { result -> result.mapLeft { ShoppingListError.FetchError }.bind().toDomain(host) }
        lists.value = detailed.sortedByName()
    }

    override suspend fun refreshList(id: String): Either<ShoppingListError, Unit> = either {
        val host = sessionDataStore.getServerAddress()
        val list = networkClient.getList(id).mapLeft { ShoppingListError.FetchError }.bind()
        upsert(list.toDomain(host))
    }

    override suspend fun createList(name: String): Either<ShoppingListError, ShoppingList> =
        either {
            val host = sessionDataStore.getServerAddress()
            val created = networkClient.createList(name)
                .mapLeft { ShoppingListError.UpdateError }.bind()
                .toDomain(host)
            upsert(created)
            created
        }

    override suspend fun renameList(id: String, name: String): Either<ShoppingListError, Unit> {
        val previousName = currentList(id)?.name ?: return Either.Left(ShoppingListError.UnknownError)
        updateList(id) { it.copy(name = name) }
        return networkClient.renameList(id, name)
            .map { }
            .mapLeft {
                updateList(id) { it.copy(name = previousName) }
                ShoppingListError.UpdateError
            }
    }

    override suspend fun deleteList(id: String): Either<ShoppingListError, Unit> {
        val snapshot = lists.value
        lists.update { all -> all?.filterNot { it.id == id } }
        return networkClient.deleteList(id).mapLeft {
            // Only restore the deleted list, so changes made to other lists meanwhile survive.
            val removed = snapshot?.firstOrNull { it.id == id }
            if (removed != null) upsert(removed)
            ShoppingListError.UpdateError
        }
    }

    override suspend fun addItem(listId: String, text: String): Either<ShoppingListError, Unit> =
        createItem(
            NewShoppingListItemDto(
                shoppingListId = listId,
                note = text,
                quantity = 1.0,
                position = nextPosition(listId),
                foodId = null,
                unitId = null,
                labelId = null,
            )
        )

    override suspend fun addFoodItem(
        listId: String,
        food: ShoppingFood,
        quantity: Double,
        unit: ShoppingUnit?,
        note: String,
    ): Either<ShoppingListError, Unit> = createItem(
        NewShoppingListItemDto(
            shoppingListId = listId,
            note = note,
            quantity = quantity,
            position = nextPosition(listId),
            foodId = food.id,
            unitId = unit?.id,
            labelId = food.labelId,
        )
    )

    override suspend fun parseIngredient(text: String): Either<ShoppingListError, ParsedIngredient> =
        networkClient.parseIngredient(text)
            .map { it.toDomain() }
            .mapLeft { ShoppingListError.FetchError }

    private suspend fun createItem(item: NewShoppingListItemDto): Either<ShoppingListError, Unit> =
        either {
            val result = networkClient.createItem(item).mapLeft { ShoppingListError.UpdateError }.bind()
            applyChanges(item.shoppingListId, result)
        }

    private fun nextPosition(listId: String): Int =
        (currentList(listId)?.items?.maxOfOrNull { it.position } ?: -1) + 1

    override suspend fun updateItem(item: ShoppingListItem): Either<ShoppingListError, Unit> =
        either {
            val result = networkClient.updateItem(item.toUpdateDto(display = ""))
                .mapLeft { ShoppingListError.UpdateError }.bind()
            applyChanges(item.listId, result)
        }

    override suspend fun setChecked(
        listId: String,
        itemIds: Set<String>,
        checked: Boolean
    ): Either<ShoppingListError, Unit> {
        val targets = currentList(listId)?.items.orEmpty()
            .filter { it.id in itemIds && it.checked != checked }
        if (targets.isEmpty()) return Either.Right(Unit)
        setCheckedLocally(listId, targets.map { it.id }.toSet(), checked)
        return networkClient.updateItems(targets.map { it.copy(checked = checked).toUpdateDto() })
            .map { applyChanges(listId, it) }
            .mapLeft {
                setCheckedLocally(listId, targets.map { it.id }.toSet(), !checked)
                ShoppingListError.UpdateError
            }
    }

    override suspend fun deleteItems(
        listId: String,
        itemIds: Set<String>
    ): Either<ShoppingListError, Unit> {
        val removed = currentList(listId)?.items.orEmpty().filter { it.id in itemIds }
        if (removed.isEmpty()) return Either.Right(Unit)
        updateList(listId) { list -> list.copy(items = list.items.filterNot { it.id in itemIds }) }
        return networkClient.deleteItems(removed.map { it.id }).mapLeft {
            updateList(listId) { list -> list.copy(items = (list.items + removed).sortedBy { it.position }) }
            ShoppingListError.UpdateError
        }
    }

    override suspend fun changeRecipeQuantity(
        listId: String,
        recipeId: String,
        delta: Int
    ): Either<ShoppingListError, Unit> {
        if (delta == 0) return Either.Right(Unit)
        val host = sessionDataStore.getServerAddress()
        val previous = currentList(listId) ?: return Either.Left(ShoppingListError.UnknownError)
        // Only the recipe's count changes up front; its items follow once the server has
        // recalculated them.
        updateList(listId) { list ->
            list.copy(recipes = list.recipes.map {
                if (it.recipeId == recipeId) it.copy(quantity = (it.quantity + delta).coerceAtLeast(0.0)) else it
            })
        }
        val amount = kotlin.math.abs(delta).toDouble()
        val result = if (delta > 0) {
            networkClient.addRecipe(listId, recipeId, amount)
        } else {
            networkClient.removeRecipe(listId, recipeId, amount)
        }
        return result
            .map { upsert(it.toDomain(host)) }
            .mapLeft {
                updateList(listId) { list -> list.copy(recipes = previous.recipes) }
                ShoppingListError.UpdateError
            }
    }

    override suspend fun addRecipe(
        listId: String,
        recipeId: String,
        scale: Double,
        ingredients: List<Ingredient>?,
    ): Either<ShoppingListError, Unit> {
        val host = sessionDataStore.getServerAddress()
        return networkClient.addRecipe(listId, recipeId, scale, ingredients?.map { it.toDto() })
            .map { upsert(it.toDomain(host)) }
            .mapLeft { ShoppingListError.UpdateError }
    }

    override suspend fun units(): Either<ShoppingListError, List<ShoppingUnit>> {
        cachedUnits?.let { return Either.Right(it) }
        return networkClient.getUnits(page = 1, perPage = ALL_ITEMS, orderBy = "name", orderDirection = "asc")
            .map { page -> page.items.map { it.toDomain() }.also { cachedUnits = it } }
            .mapLeft { ShoppingListError.FetchError }
    }

    override suspend fun searchFoods(query: String): Either<ShoppingListError, List<ShoppingFood>> =
        networkClient.searchFoods(query = query, page = 1, perPage = FOOD_SEARCH_PAGE_SIZE)
            .map { page -> page.items.map { it.toDomain() } }
            .mapLeft { ShoppingListError.FetchError }

    private fun currentList(id: String): ShoppingList? = lists.value?.firstOrNull { it.id == id }

    private fun upsert(list: ShoppingList) {
        lists.update { all ->
            ((all ?: emptyList()).filterNot { it.id == list.id } + list).sortedByName()
        }
    }

    private fun updateList(id: String, transform: (ShoppingList) -> ShoppingList) {
        lists.update { all -> all?.map { if (it.id == id) transform(it) else it } }
    }

    private fun setCheckedLocally(listId: String, ids: Set<String>, checked: Boolean) {
        updateList(listId) { list ->
            list.copy(items = list.items.map { if (it.id in ids) it.copy(checked = checked) else it })
        }
    }

    /** Folds an item write's result into the list, so it needn't be fetched again. */
    private fun applyChanges(listId: String, changes: ShoppingListItemsCollectionDto) {
        val deleted = changes.deletedItems.map { it.id }.toSet()
        val changed = (changes.createdItems + changes.updatedItems)
            .filter { it.shoppingListId == listId }
            .map { it.toDomain() }
        val changedIds = changed.map { it.id }.toSet()
        updateList(listId) { list ->
            list.copy(
                items = (list.items.filterNot { it.id in deleted || it.id in changedIds } + changed)
                    .sortedBy { it.position }
            )
        }
    }

    private fun List<ShoppingList>.sortedByName() = sortedBy { it.name.lowercase() }
}
