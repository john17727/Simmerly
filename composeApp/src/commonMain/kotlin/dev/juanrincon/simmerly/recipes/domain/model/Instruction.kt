package dev.juanrincon.simmerly.recipes.domain.model

data class Instruction(
    val id: String,
    val title: String,
    val summary: String,
    val text: String,
    val associatedIngredients: List<String>
)
