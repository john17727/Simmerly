package dev.juanrincon.simmerly.recipes.presentation.details.models

import dev.juanrincon.simmerly.recipes.domain.model.Settings

data class RecipeDetailUi(
    val id: String,
    val title: String,
    val image: String,
    val ingredients: List<IngredientUi>,
    val settings: Settings
)
