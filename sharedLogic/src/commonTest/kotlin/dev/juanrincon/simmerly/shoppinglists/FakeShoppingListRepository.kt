package dev.juanrincon.simmerly.shoppinglists

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListError
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.domain.model.ParsedIngredient
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingFood
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingListItem
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeShoppingListRepository : ShoppingListRepository {
    val lists = MutableStateFlow<List<ShoppingList>?>(null)

    var refreshListsResult: Either<ShoppingListError, Unit> = Unit.right()
    var refreshListResult: Either<ShoppingListError, Unit> = Unit.right()
    var createListResult: Either<ShoppingListError, ShoppingList>? = null
    var writeResult: Either<ShoppingListError, Unit> = Unit.right()
    var unitsResult: Either<ShoppingListError, List<ShoppingUnit>> = emptyList<ShoppingUnit>().right()
    var foodsResult: Either<ShoppingListError, List<ShoppingFood>> = emptyList<ShoppingFood>().right()
    var parseResult: Either<ShoppingListError, ParsedIngredient> = ShoppingListError.FetchError.left()

    val createdNames = mutableListOf<String>()
    val renamed = mutableListOf<Pair<String, String>>()
    val deletedLists = mutableListOf<String>()
    val addedItems = mutableListOf<Pair<String, String>>()
    val updatedItems = mutableListOf<ShoppingListItem>()
    val checkedCalls = mutableListOf<Triple<String, Set<String>, Boolean>>()
    val deletedItems = mutableListOf<Set<String>>()
    val recipeQuantityChanges = mutableListOf<Pair<String, Int>>()
    /** Every [changeRecipeQuantity] call as (listId, recipeId, delta). */
    val recipeQuantityCalls = mutableListOf<Triple<String, String, Int>>()
    val addedRecipes = mutableListOf<AddedRecipe>()
    val foodQueries = mutableListOf<String>()
    val parsedTexts = mutableListOf<String>()
    val addedFoodItems = mutableListOf<AddedFoodItem>()

    data class AddedRecipe(val listId: String, val recipeId: String, val scale: Double, val ingredients: List<Ingredient>?)

    data class AddedFoodItem(val food: ShoppingFood, val quantity: Double, val unit: ShoppingUnit?, val note: String)

    override fun observeLists(): Flow<List<ShoppingList>?> = lists

    override fun observeList(id: String): Flow<ShoppingList?> =
        lists.map { all -> all?.firstOrNull { it.id == id } }

    override suspend fun refreshLists() = refreshListsResult

    override suspend fun refreshList(id: String) = refreshListResult

    override suspend fun createList(name: String): Either<ShoppingListError, ShoppingList> {
        createdNames += name
        return createListResult ?: aShoppingList(id = "new-list", name = name).right()
    }

    override suspend fun renameList(id: String, name: String): Either<ShoppingListError, Unit> {
        renamed += id to name
        return writeResult
    }

    override suspend fun deleteList(id: String): Either<ShoppingListError, Unit> {
        deletedLists += id
        return writeResult
    }

    override suspend fun addItem(listId: String, text: String): Either<ShoppingListError, Unit> {
        addedItems += listId to text
        return writeResult
    }

    override suspend fun addFoodItem(
        listId: String,
        food: ShoppingFood,
        quantity: Double,
        unit: ShoppingUnit?,
        note: String,
    ): Either<ShoppingListError, Unit> {
        addedFoodItems += AddedFoodItem(food, quantity, unit, note)
        return writeResult
    }

    override suspend fun parseIngredient(text: String): Either<ShoppingListError, ParsedIngredient> {
        parsedTexts += text
        return parseResult
    }

    override suspend fun updateItem(item: ShoppingListItem): Either<ShoppingListError, Unit> {
        updatedItems += item
        return writeResult
    }

    override suspend fun setChecked(
        listId: String,
        itemIds: Set<String>,
        checked: Boolean
    ): Either<ShoppingListError, Unit> {
        checkedCalls += Triple(listId, itemIds, checked)
        return writeResult
    }

    override suspend fun deleteItems(listId: String, itemIds: Set<String>): Either<ShoppingListError, Unit> {
        deletedItems += itemIds
        return writeResult
    }

    override suspend fun changeRecipeQuantity(
        listId: String,
        recipeId: String,
        delta: Int
    ): Either<ShoppingListError, Unit> {
        recipeQuantityChanges += recipeId to delta
        recipeQuantityCalls += Triple(listId, recipeId, delta)
        return writeResult
    }

    override suspend fun addRecipe(
        listId: String,
        recipeId: String,
        scale: Double,
        ingredients: List<Ingredient>?,
    ): Either<ShoppingListError, Unit> {
        addedRecipes += AddedRecipe(listId, recipeId, scale, ingredients)
        return writeResult
    }

    override suspend fun units() = unitsResult

    override suspend fun searchFoods(query: String): Either<ShoppingListError, List<ShoppingFood>> {
        foodQueries += query
        return foodsResult
    }
}
