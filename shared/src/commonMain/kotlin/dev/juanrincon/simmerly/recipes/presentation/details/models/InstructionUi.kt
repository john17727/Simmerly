package dev.juanrincon.simmerly.recipes.presentation.details.models

data class InstructionUi(
    val id: String,
    val title: String?,
    val summary: String,
    val text: String,
    val associatedIngredients: List<IngredientUi>
)
