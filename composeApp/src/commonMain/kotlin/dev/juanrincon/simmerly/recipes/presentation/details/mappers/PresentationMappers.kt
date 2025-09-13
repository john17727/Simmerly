package dev.juanrincon.simmerly.recipes.presentation.details.mappers

import dev.juanrincon.simmerly.core.utils.capitalizeWords
import dev.juanrincon.simmerly.core.utils.format
import dev.juanrincon.simmerly.core.utils.nullIfEmpty
import dev.juanrincon.simmerly.recipes.domain.model.Food
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.Unit
import dev.juanrincon.simmerly.recipes.presentation.details.models.FoodUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.UnitUi

fun RecipeDetail.toRecipeDetailUi(): RecipeDetailUi = RecipeDetailUi(
    id = id,
    title = name,
    image = image,
    servings = servings,
    ingredients = ingredients.map { it.toIngredientUi() },
    settings = settings
)

fun Ingredient.toIngredientUi(): IngredientUi = IngredientUi(
    quantity = quantity,
    display = display,
    food = food?.toFoodUi(),
    unit = unit?.toUnitUi(),
    note = if (display == note) null else note?.nullIfEmpty()
)

fun Food.toFoodUi(): FoodUi = FoodUi(
    name = name,
    pluralName = pluralName
)

fun Unit.toUnitUi(): UnitUi = UnitUi(
    name = name,
    pluralName = pluralName,
    fraction = fraction,
    abbreviation = abbreviation,
    pluralAbbreviation = pluralAbbreviation,
    useAbbreviation = useAbbreviation
)