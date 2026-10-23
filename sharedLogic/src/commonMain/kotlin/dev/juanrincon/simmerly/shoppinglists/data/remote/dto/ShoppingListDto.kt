package dev.juanrincon.simmerly.shoppinglists.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Mealie's `ShoppingListOut`, also used to decode `ShoppingListSummary` from the list endpoint —
 * that variant simply has no `listItems`, so it decodes with an empty list.
 *
 * Every field Mealie may send as `null` is nullable with a default, so a list created by another
 * client (or one with missing timestamps) never fails to decode.
 */
@Serializable
data class ShoppingListDto(
    val id: String,
    val name: String? = null,
    val userId: String,
    val groupId: String,
    val householdId: String,
    val listItems: List<ShoppingListItemDto> = emptyList(),
    val recipeReferences: List<ShoppingListRecipeRefDto> = emptyList(),
    val labelSettings: List<ShoppingListLabelSettingDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
