package dev.juanrincon.simmerly.shoppinglists.data.remote.dto

import dev.juanrincon.simmerly.recipes.data.remote.dto.LabelDto
import kotlinx.serialization.Serializable

/** The per-list order of a label (an aisle), which sets the order of the list's sections. */
@Serializable
data class ShoppingListLabelSettingDto(
    val id: String,
    val shoppingListId: String,
    val labelId: String,
    val position: Int = 0,
    val label: LabelDto,
)
