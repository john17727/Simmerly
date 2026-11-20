package dev.juanrincon.simmerly.shoppinglists.data.mappers

import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingFoodColumns
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingLabelColumns
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingListEntity
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingListItemEntity
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingListRecipeEntity
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingListWithRelations
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingUnitColumns
import dev.juanrincon.simmerly.shoppinglists.domain.model.ItemRecipeRef
import dev.juanrincon.simmerly.shoppinglists.domain.model.LinkedRecipe
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingFood
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingLabel
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingListItem
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** An item's per-recipe amounts as stored in the item row's `recipe_refs` column. */
@Serializable
private data class StoredRecipeRef(
    val id: String,
    val recipeId: String,
    val quantity: Double,
    val scale: Double? = null,
    val note: String? = null,
)

private val json = Json { ignoreUnknownKeys = true }
private val labelOrderSerializer = ListSerializer(String.serializer())
private val recipeRefsSerializer = ListSerializer(StoredRecipeRef.serializer())

fun ShoppingListWithRelations.toDomain(): ShoppingList = ShoppingList(
    id = list.id,
    name = list.name,
    userId = list.userId,
    items = items.sortedWith(compareBy({ it.position }, { it.id })).map { it.toDomain() },
    recipes = recipes.sortedBy { it.position }.map {
        LinkedRecipe(
            recipeId = it.recipeId,
            name = it.name,
            slug = it.slug,
            imageUrl = it.imageUrl,
            quantity = it.quantity,
        )
    },
    labelOrder = json.decodeFromString(labelOrderSerializer, list.labelOrder),
)

fun ShoppingList.toEntity() = ShoppingListEntity(
    id = id,
    name = name,
    userId = userId,
    labelOrder = json.encodeToString(labelOrderSerializer, labelOrder),
)

fun ShoppingList.recipeEntities(): List<ShoppingListRecipeEntity> = recipes.mapIndexed { index, recipe ->
    ShoppingListRecipeEntity(
        listId = id,
        recipeId = recipe.recipeId,
        position = index,
        name = recipe.name,
        slug = recipe.slug,
        imageUrl = recipe.imageUrl,
        quantity = recipe.quantity,
    )
}

fun ShoppingList.itemEntities(): List<ShoppingListItemEntity> = items.map { it.toEntity() }

private fun ShoppingListItemEntity.toDomain() = ShoppingListItem(
    id = id,
    listId = listId,
    quantity = quantity,
    unit = unit?.let { ShoppingUnit(it.id, it.name, it.pluralName, it.abbreviation, it.useAbbreviation) },
    food = food?.let { ShoppingFood(it.id, it.name, it.pluralName, it.labelId, it.labelName) },
    note = note,
    display = display,
    checked = checked,
    position = position,
    labelId = labelId,
    label = label?.let { ShoppingLabel(it.id, it.name, it.color) },
    recipeRefs = json.decodeFromString(recipeRefsSerializer, recipeRefs).map {
        ItemRecipeRef(it.id, it.recipeId, it.quantity, it.scale, it.note)
    },
)

private fun ShoppingListItem.toEntity() = ShoppingListItemEntity(
    id = id,
    listId = listId,
    quantity = quantity,
    note = note,
    display = display,
    checked = checked,
    position = position,
    labelId = labelId,
    unit = unit?.let { ShoppingUnitColumns(it.id, it.name, it.pluralName, it.abbreviation, it.useAbbreviation) },
    food = food?.let { ShoppingFoodColumns(it.id, it.name, it.pluralName, it.labelId, it.labelName) },
    label = label?.let { ShoppingLabelColumns(it.id, it.name, it.color) },
    recipeRefs = json.encodeToString(
        recipeRefsSerializer,
        recipeRefs.map { StoredRecipeRef(it.id, it.recipeId, it.quantity, it.scale, it.note) }
    ),
)
