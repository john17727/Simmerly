package dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing

import kotlinx.serialization.Serializable

/**
 * Mealie's `ShoppingListItemUpdateBulk`; the single-item endpoint takes the same shape and ignores
 * [id]. An update replaces the whole item, so every field is sent — there are deliberately no
 * defaults, because the client's `Json` drops properties equal to their default and the server
 * would then reset them (an omitted `checked` would uncheck the item).
 */
@Serializable
data class ShoppingListItemUpdateDto(
    val id: String,
    val shoppingListId: String,
    val quantity: Double,
    val unitId: String?,
    val foodId: String?,
    val labelId: String?,
    val note: String?,
    val display: String,
    val checked: Boolean,
    val position: Int,
    val recipeReferences: List<ShoppingListItemRecipeRefUpdateDto>,
)

@Serializable
data class ShoppingListItemRecipeRefUpdateDto(
    val id: String,
    val shoppingListItemId: String,
    val recipeId: String,
    val recipeQuantity: Double,
    val recipeScale: Double?,
    val recipeNote: String?,
)
