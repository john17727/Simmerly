package dev.juanrincon.simmerly.shoppinglists.data.remote.dto

import dev.juanrincon.simmerly.recipes.data.remote.dto.LabelDto
import kotlinx.serialization.Serializable

/**
 * The fields of Mealie's `IngredientFood` a shopping list uses. Kept apart from the recipes
 * `FoodDto`, whose non-null timestamps don't hold for every food this API returns.
 */
@Serializable
data class ShoppingFoodDto(
    val id: String,
    val name: String,
    val pluralName: String? = null,
    val labelId: String? = null,
    val label: LabelDto? = null,
)
