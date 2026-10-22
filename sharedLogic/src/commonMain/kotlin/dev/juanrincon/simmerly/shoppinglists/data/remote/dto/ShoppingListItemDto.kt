package dev.juanrincon.simmerly.shoppinglists.data.remote.dto

import dev.juanrincon.simmerly.recipes.data.remote.dto.LabelDto
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListItemDto(
    val id: String,
    val shoppingListId: String,
    val quantity: Double = 1.0,
    val unit: ShoppingUnitDto? = null,
    val food: ShoppingFoodDto? = null,
    val note: String? = null,
    val display: String = "",
    val checked: Boolean = false,
    val position: Int = 0,
    val foodId: String? = null,
    val labelId: String? = null,
    val unitId: String? = null,
    val label: LabelDto? = null,
    val recipeReferences: List<ShoppingListItemRecipeRefDto> = emptyList(),
)
