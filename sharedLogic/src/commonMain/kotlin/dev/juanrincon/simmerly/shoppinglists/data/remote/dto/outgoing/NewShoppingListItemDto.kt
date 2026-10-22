package dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing

import kotlinx.serialization.Serializable

/**
 * A new item: either note-only, the way Mealie's own UI adds free text, or tied to a food (and
 * optionally a unit), with the food's label so it lands in that aisle. The ids have no defaults
 * so a `null` is sent explicitly rather than dropped.
 */
@Serializable
data class NewShoppingListItemDto(
    val shoppingListId: String,
    val note: String,
    val quantity: Double,
    val position: Int,
    val foodId: String?,
    val unitId: String?,
    val labelId: String?,
)
