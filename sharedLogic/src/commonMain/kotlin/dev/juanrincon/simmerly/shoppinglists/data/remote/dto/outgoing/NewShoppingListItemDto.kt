package dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing

import kotlinx.serialization.Serializable

/** A note-only item, the way Mealie's own UI adds free text. */
@Serializable
data class NewShoppingListItemDto(
    val shoppingListId: String,
    val note: String,
    val quantity: Double,
    val position: Int,
)
