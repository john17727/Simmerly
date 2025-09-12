package dev.juanrincon.simmerly.recipes.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class IngredientDto(
    val quantity: Double,
    val unit: UnitDto?,
    val food: FoodDto?,
    val note: String?,
    val isFood: Boolean,
    val disableAmount: Boolean,
    val display: String,
    val title: String?,
    val originalText: String?,
    val referenceId: String
)