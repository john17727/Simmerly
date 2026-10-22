package dev.juanrincon.simmerly.shoppinglists.data.remote.dto

import kotlinx.serialization.Serializable

/** The fields of Mealie's `IngredientUnit` a shopping list uses — see [ShoppingFoodDto]. */
@Serializable
data class ShoppingUnitDto(
    val id: String,
    val name: String,
    val pluralName: String? = null,
    val abbreviation: String = "",
    val pluralAbbreviation: String? = null,
    val useAbbreviation: Boolean = false,
)
