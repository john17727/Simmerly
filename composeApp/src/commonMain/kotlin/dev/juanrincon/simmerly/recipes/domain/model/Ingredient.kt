package dev.juanrincon.simmerly.recipes.domain.model

data class Ingredient(
    val quantity: Double,
    val unit: Unit?,
    val food: Food?,
    val note: String?,
    val display: String,
    val title: String?,
    val originalText: String?,
    val referenceId: String
)
