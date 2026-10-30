package dev.juanrincon.simmerly.shoppinglists.data.mappers

import dev.juanrincon.simmerly.core.data.remote.recipeImageUrl
import dev.juanrincon.simmerly.recipes.data.remote.dto.LabelDto
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.ParsedIngredientDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.ShoppingFoodDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.ShoppingListDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.ShoppingListItemDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.ShoppingUnitDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.RecipeIngredientDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.RecipeIngredientFoodDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.RecipeIngredientUnitDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.ShoppingListItemRecipeRefUpdateDto
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.ShoppingListItemUpdateDto
import dev.juanrincon.simmerly.shoppinglists.domain.model.ItemRecipeRef
import dev.juanrincon.simmerly.shoppinglists.domain.model.LinkedRecipe
import dev.juanrincon.simmerly.shoppinglists.domain.model.ParsedIngredient
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingFood
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingLabel
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingListItem
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingUnit

fun ShoppingListDto.toDomain(host: String?): ShoppingList = ShoppingList(
    id = id,
    name = name.orEmpty(),
    userId = userId,
    items = listItems.map { it.toDomain() },
    recipes = recipeReferences.map { ref ->
        LinkedRecipe(
            recipeId = ref.recipeId,
            name = ref.recipe.name.orEmpty(),
            slug = ref.recipe.slug,
            imageUrl = recipeImageUrl(host, ref.recipeId),
            quantity = ref.recipeQuantity,
        )
    },
    labelOrder = labelSettings.sortedBy { it.position }.map { it.labelId },
)

fun ShoppingListItemDto.toDomain(): ShoppingListItem = ShoppingListItem(
    id = id,
    listId = shoppingListId,
    quantity = quantity,
    unit = unit?.toDomain(),
    food = food?.toDomain(),
    note = note.orEmpty(),
    display = display,
    checked = checked,
    position = position,
    labelId = labelId ?: label?.id,
    label = label?.toDomain(),
    recipeRefs = recipeReferences.map {
        ItemRecipeRef(
            id = it.id,
            recipeId = it.recipeId,
            quantity = it.recipeQuantity,
            scale = it.recipeScale,
            note = it.recipeNote,
        )
    },
)

fun ShoppingUnitDto.toDomain(): ShoppingUnit = ShoppingUnit(
    id = id,
    name = name,
    pluralName = pluralName,
    abbreviation = abbreviation,
    useAbbreviation = useAbbreviation,
)

fun ShoppingFoodDto.toDomain(): ShoppingFood = ShoppingFood(
    id = id,
    name = name,
    pluralName = pluralName,
    labelId = labelId ?: label?.id,
    labelName = label?.name,
)

fun ParsedIngredientDto.toDomain(): ParsedIngredient = with(ingredient) {
    ParsedIngredient(
        quantity = quantity,
        unit = unit?.id?.let { id ->
            ShoppingUnit(
                id = id,
                name = unit.name,
                pluralName = unit.pluralName,
                abbreviation = unit.abbreviation,
                useAbbreviation = unit.useAbbreviation,
            )
        },
        unitName = unit?.name,
        food = food?.id?.let { id ->
            ShoppingFood(
                id = id,
                name = food.name,
                pluralName = food.pluralName,
                labelId = food.labelId ?: food.label?.id,
                labelName = food.label?.name,
            )
        },
        foodName = food?.name,
        note = note.orEmpty(),
    )
}

fun LabelDto.toDomain(): ShoppingLabel = ShoppingLabel(id = id, name = name, color = color)

/**
 * The full update body for [this] item. [display] is sent empty when the item's food, unit,
 * quantity or note changed, so the server formats it afresh instead of keeping stale text.
 */
fun ShoppingListItem.toUpdateDto(display: String = this.display): ShoppingListItemUpdateDto =
    ShoppingListItemUpdateDto(
        id = id,
        shoppingListId = listId,
        quantity = quantity,
        unitId = unit?.id,
        foodId = food?.id,
        labelId = labelId,
        note = note,
        display = display,
        checked = checked,
        position = position,
        recipeReferences = recipeRefs.map {
            ShoppingListItemRecipeRefUpdateDto(
                id = it.id,
                shoppingListItemId = id,
                recipeId = it.recipeId,
                recipeQuantity = it.quantity,
                recipeScale = it.scale,
                recipeNote = it.note,
            )
        },
    )

fun Ingredient.toDto(): RecipeIngredientDto = RecipeIngredientDto(
    quantity = quantity,
    unit = unit?.let {
        RecipeIngredientUnitDto(
            id = it.id,
            name = it.name,
            pluralName = it.pluralName,
            abbreviation = it.abbreviation,
            pluralAbbreviation = it.pluralAbbreviation,
            useAbbreviation = it.useAbbreviation,
            fraction = it.fraction,
        )
    },
    food = food?.let {
        RecipeIngredientFoodDto(id = it.id, name = it.name, pluralName = it.pluralName, labelId = it.labelId)
    },
    note = note,
    display = display,
    title = title,
    originalText = originalText,
    referenceId = referenceId,
)
