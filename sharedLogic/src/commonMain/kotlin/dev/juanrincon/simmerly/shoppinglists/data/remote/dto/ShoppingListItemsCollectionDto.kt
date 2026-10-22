package dev.juanrincon.simmerly.shoppinglists.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * What every item write returns. Mealie merges an item into an existing one with the same food and
 * unit, so a create can come back under [updatedItems] rather than [createdItems].
 */
@Serializable
data class ShoppingListItemsCollectionDto(
    val createdItems: List<ShoppingListItemDto> = emptyList(),
    val updatedItems: List<ShoppingListItemDto> = emptyList(),
    val deletedItems: List<ShoppingListItemDto> = emptyList(),
)
