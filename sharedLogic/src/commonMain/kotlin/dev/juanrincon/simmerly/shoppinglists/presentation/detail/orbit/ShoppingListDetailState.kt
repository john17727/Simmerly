package dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit

import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.FoodOptionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemEditorUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemGrouping
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemSectionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.LinkedRecipeUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ShoppingItemUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.UnitOptionUi

data class ShoppingListDetailState(
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val name: String = "",
    val remainingCount: Int = 0,
    val checkedCount: Int = 0,
    val progress: Float = 0f,
    val linkedRecipes: List<LinkedRecipeUi> = emptyList(),
    val grouping: ItemGrouping = ItemGrouping.ByAisle,
    /** The unchecked items, grouped by [grouping]. */
    val sections: List<ItemSectionUi> = emptyList(),
    val checkedItems: List<ShoppingItemUi> = emptyList(),
    val checkedExpanded: Boolean = true,
    val addText: String = "",
    val isAdding: Boolean = false,
    val editor: ItemEditorUi? = null,
    val units: List<UnitOptionUi> = emptyList(),
    val foodSuggestions: List<FoodOptionUi> = emptyList(),
    val dialog: DetailDialog = DetailDialog.None,
)

sealed interface DetailDialog {
    data object None : DetailDialog
    data class Rename(val currentName: String) : DetailDialog
    data object ConfirmDelete : DetailDialog
}
