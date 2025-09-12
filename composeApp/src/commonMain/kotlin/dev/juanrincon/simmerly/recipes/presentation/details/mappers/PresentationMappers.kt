package dev.juanrincon.simmerly.recipes.presentation.details.mappers

import dev.juanrincon.simmerly.core.utils.capitalizeWords
import dev.juanrincon.simmerly.core.utils.format
import dev.juanrincon.simmerly.core.utils.nullIfEmpty
import dev.juanrincon.simmerly.recipes.domain.model.Food
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.Unit
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi

fun RecipeDetail.toRecipeDetailUi(): RecipeDetailUi = RecipeDetailUi(
    id = id,
    title = name,
    image = image,
    ingredients = ingredients.map { it.toIngredientUi() },
    settings = settings
)

fun Ingredient.toIngredientUi(): IngredientUi {
    return if (isFood) {
        IngredientUi(
            quantity = buildQuantityString(quantity, unit),
            name = buildIngredientString(quantity, food),
            note = note?.nullIfEmpty()
        )
    } else {
        IngredientUi(
            quantity = null,
            name = display,
            note = note?.nullIfEmpty()
        )
    }
}

private fun buildQuantityString(quantity: Double, unit: Unit?): String {
    val formattedQuantity = quantity.format(1)
    val formattedUnit = if (quantity > 1.0) {
        (unit?.pluralAbbreviation ?: unit?.abbreviation)?.nullIfEmpty()
    } else {
        unit?.abbreviation?.nullIfEmpty()
    }
    return if (formattedUnit == null) {
        formattedQuantity
    } else {
        "$formattedQuantity $formattedUnit"
    }
}

private fun buildIngredientString(quantity: Double, food: Food?): String = if (quantity > 1.0) {
    (food?.pluralName ?: food?.name ?: "").capitalizeWords()
} else {
    (food?.name ?: "").capitalizeWords()
}