package dev.juanrincon.simmerly.recipes.presentation.details.models

import dev.juanrincon.simmerly.recipes.domain.model.Ingredient

data class InstructionUi(
    val id: String,
    val title: String,
    val summary: String?,
    val text: String,
    val associatedIngredients: List<IngredientUi>
)
