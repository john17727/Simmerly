package dev.juanrincon.simmerly.shoppinglists.data

import arrow.core.Either
import arrow.core.raise.either
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.data.optimistic
import dev.juanrincon.simmerly.recipes.data.local.recipe.UnitDao
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingListStore
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
import kotlinx.coroutines.flow.distinctUntilChanged

/** Every list or unit fits in one page when Mealie is asked for `perPage = -1`. */
private const val ALL_ITEMS = -1

/** How many foods the add-item and editor searches show. */
private const val FOOD_SEARCH_PAGE_SIZE = 50

class DefaultShoppingListRepository(
    private val networkClient: ShoppingListNetworkClient,
    private val sessionDataStore: SessionDataStore,
    private val store: ShoppingListStore,
    private val unitDao: UnitDao,
) : ShoppingListRepository {

    override fun observeLists(): Flow<List<ShoppingList>?> = store.observeAllOrNull()

    override fun observeList(id: String): Flow<ShoppingList?> = store.observe(id).distinctUntilChanged()

    override suspend fun refreshLists(): Either<ShoppingListError, Unit> = either {
        val host = sessionDataStore.getServerAddress()
        val summaries = networkClient.getLists(page = 1, perPage = ALL_ITEMS, orderBy = "name", orderDirection = "asc")
            .mapLeft { ShoppingListError.FetchError }.bind()
        val detailed = coroutineScope {
            summaries.items.map { summary -> async { networkClient.getList(summary.id) } }
                .awaitAll()
        }.map { result -> result.mapLeft { ShoppingListError.FetchError }.bind().toDomain(host) }
        store.replaceAll(detailed)
    }

    override suspend fun refreshList(id: String): Either<ShoppingListError, Unit> = either {
        val host = sessionDataStore.getServerAddress()
        val list = networkClient.getList(id).mapLeft { ShoppingListError.FetchError }.bind()
        store.upsert(list.toDomain(host))
    }

    override suspend fun createList(name: String): Either<ShoppingListError, ShoppingList> =
        either {
            val host = sessionDataStore.getServerAddress()
            val created = networkClient.createList(name)
                .mapLeft { ShoppingListError.UpdateError }.bind()
                .toDomain(host)
            store.upsert(created)
            created
        }

    override suspend fun renameList(id: String, name: String): Either<ShoppingListError, Unit> {
        val previousName = store.get(id)?.name ?: return Either.Left(ShoppingListError.UnknownError)
        return optimistic(
            apply = { store.update(id) { it.copy(name = name) } },
            remote = { networkClient.renameList(id, name) },
            rollback = { store.update(id) { it.copy(name = previousName) } },
            mapError = { ShoppingListError.UpdateError },
        ).map { }
    }

    override suspend fun deleteList(id: String): Either<ShoppingListError, Unit> {
        val removed = store.get(id)
        return optimistic(
            apply = { store.delete(id) },
            remote = { networkClient.deleteList(id) },
            // Only restore the deleted list, so changes made to other lists meanwhile survive.
            rollback = { if (removed != null) store.upsert(removed) },
            mapError = { ShoppingListError.UpdateError },
        ).map { }
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

    private suspend fun nextPosition(listId: String): Int =
        (store.get(listId)?.items?.maxOfOrNull { it.position } ?: -1) + 1

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
        val targets = store.get(listId)?.items.orEmpty()
            .filter { it.id in itemIds && it.checked != checked }
        if (targets.isEmpty()) return Either.Right(Unit)
        val targetIds = targets.map { it.id }.toSet()
        return optimistic(
            apply = { setCheckedLocally(listId, targetIds, checked) },
            remote = { networkClient.updateItems(targets.map { it.copy(checked = checked).toUpdateDto() }) },
            rollback = { setCheckedLocally(listId, targetIds, !checked) },
            onSuccess = { applyChanges(listId, it) },
            mapError = { ShoppingListError.UpdateError },
        ).map { }
    }

    override suspend fun deleteItems(
        listId: String,
        itemIds: Set<String>
    ): Either<ShoppingListError, Unit> {
        val removed = store.get(listId)?.items.orEmpty().filter { it.id in itemIds }
        if (removed.isEmpty()) return Either.Right(Unit)
        return optimistic(
            apply = {
                store.update(listId) { list -> list.copy(items = list.items.filterNot { it.id in itemIds }) }
            },
            remote = { networkClient.deleteItems(removed.map { it.id }) },
            rollback = {
                store.update(listId) { list ->
                    list.copy(items = (list.items + removed).sortedBy { it.position })
                }
            },
            mapError = { ShoppingListError.UpdateError },
        ).map { }
    }

    override suspend fun changeRecipeQuantity(
        listId: String,
        recipeId: String,
        delta: Int
    ): Either<ShoppingListError, Unit> {
        if (delta == 0) return Either.Right(Unit)
        val host = sessionDataStore.getServerAddress()
        val previous = store.get(listId) ?: return Either.Left(ShoppingListError.UnknownError)
        val amount = kotlin.math.abs(delta).toDouble()
        return optimistic(
            // Only the recipe's count changes up front; its items follow once the server has
            // recalculated them.
            apply = {
                store.update(listId) { list ->
                    list.copy(recipes = list.recipes.map {
                        if (it.recipeId == recipeId) it.copy(quantity = (it.quantity + delta).coerceAtLeast(0.0)) else it
                    })
                }
            },
            remote = {
                if (delta > 0) networkClient.addRecipe(listId, recipeId, amount)
                else networkClient.removeRecipe(listId, recipeId, amount)
            },
            rollback = { store.update(listId) { list -> list.copy(recipes = previous.recipes) } },
            onSuccess = { store.upsert(it.toDomain(host)) },
            mapError = { ShoppingListError.UpdateError },
        ).map { }
    }

    override suspend fun addRecipe(
        listId: String,
        recipeId: String,
        scale: Double,
        ingredients: List<Ingredient>?,
    ): Either<ShoppingListError, Unit> {
        val host = sessionDataStore.getServerAddress()
        return networkClient.addRecipe(listId, recipeId, scale, ingredients?.map { it.toDto() })
            .map { store.upsert(it.toDomain(host)) }
            .mapLeft { ShoppingListError.UpdateError }
    }

    override suspend fun units(): Either<ShoppingListError, List<ShoppingUnit>> {
        // The initial load already keeps the units table; only an empty one needs the network.
        val stored = unitDao.getAll()
        if (stored.isNotEmpty()) {
            return Either.Right(stored.map {
                ShoppingUnit(it.id, it.name, it.pluralName, it.abbreviation, it.useAbbreviation)
            })
        }
        return networkClient.getUnits(page = 1, perPage = ALL_ITEMS, orderBy = "name", orderDirection = "asc")
            .map { page -> page.items.map { it.toDomain() } }
            .mapLeft { ShoppingListError.FetchError }
    }

    override suspend fun searchFoods(query: String): Either<ShoppingListError, List<ShoppingFood>> =
        networkClient.searchFoods(query = query, page = 1, perPage = FOOD_SEARCH_PAGE_SIZE)
            .map { page -> page.items.map { it.toDomain() } }
            .mapLeft { ShoppingListError.FetchError }

    private suspend fun setCheckedLocally(listId: String, ids: Set<String>, checked: Boolean) {
        store.update(listId) { list ->
            list.copy(items = list.items.map { if (it.id in ids) it.copy(checked = checked) else it })
        }
    }

    /** Folds an item write's result into the list, so it needn't be fetched again. */
    private suspend fun applyChanges(listId: String, changes: ShoppingListItemsCollectionDto) {
        val deleted = changes.deletedItems.map { it.id }.toSet()
        val changed = (changes.createdItems + changes.updatedItems)
            .filter { it.shoppingListId == listId }
            .map { it.toDomain() }
        val changedIds = changed.map { it.id }.toSet()
        store.update(listId) { list ->
            list.copy(
                items = (list.items.filterNot { it.id in deleted || it.id in changedIds } + changed)
                    .sortedBy { it.position }
            )
        }
    }
}
