package dev.juanrincon.simmerly.shoppinglists.domain

import arrow.core.Either
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.shoppinglists.domain.model.ParsedIngredient
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingFood
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingListItem
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingUnit
import kotlinx.coroutines.flow.Flow

/**
 * Shopping lists, kept in memory and refreshed from the network — there is no offline cache.
 * Writes that the user expects to see at once (checking, deleting, renaming) are applied to the
 * observed lists before the request and reverted if it fails.
 */
interface ShoppingListRepository {
    /** Every list loaded so far, in the server's name order; `null` until the first refresh. */
    fun observeLists(): Flow<List<ShoppingList>?>

    fun observeList(id: String): Flow<ShoppingList?>

    /** Reloads every list with its items — the overview needs item counts the list endpoint
     * doesn't return, so each list is fetched individually. */
    suspend fun refreshLists(): Either<ShoppingListError, Unit>

    suspend fun refreshList(id: String): Either<ShoppingListError, Unit>

    suspend fun createList(name: String): Either<ShoppingListError, ShoppingList>

    suspend fun renameList(id: String, name: String): Either<ShoppingListError, Unit>

    suspend fun deleteList(id: String): Either<ShoppingListError, Unit>

    /** Adds [text] as a note-only item. */
    suspend fun addItem(listId: String, text: String): Either<ShoppingListError, Unit>

    /** Adds an item for [food], filed under the food's aisle. */
    suspend fun addFoodItem(
        listId: String,
        food: ShoppingFood,
        quantity: Double,
        unit: ShoppingUnit?,
        note: String,
    ): Either<ShoppingListError, Unit>

    /** Splits free text like "2 cups milk" with Mealie's ingredient parser. Fails when the
     * server's parser is unavailable. */
    suspend fun parseIngredient(text: String): Either<ShoppingListError, ParsedIngredient>

    suspend fun updateItem(item: ShoppingListItem): Either<ShoppingListError, Unit>

    suspend fun setChecked(
        listId: String,
        itemIds: Set<String>,
        checked: Boolean
    ): Either<ShoppingListError, Unit>

    suspend fun deleteItems(listId: String, itemIds: Set<String>): Either<ShoppingListError, Unit>

    /** Adds ([delta] > 0) or removes ([delta] < 0) servings of a linked recipe's ingredients. */
    suspend fun changeRecipeQuantity(
        listId: String,
        recipeId: String,
        delta: Int
    ): Either<ShoppingListError, Unit>

    /**
     * Links a recipe to the list and adds its ingredients, scaled by [scale] (1 = the recipe's own
     * servings). [ingredients] limits it to those ingredients; `null` adds them all. Like Mealie's
     * web app, the recipe is linked either way.
     */
    suspend fun addRecipe(
        listId: String,
        recipeId: String,
        scale: Double,
        ingredients: List<Ingredient>? = null,
    ): Either<ShoppingListError, Unit>

    suspend fun units(): Either<ShoppingListError, List<ShoppingUnit>>

    suspend fun searchFoods(query: String): Either<ShoppingListError, List<ShoppingFood>>
}
