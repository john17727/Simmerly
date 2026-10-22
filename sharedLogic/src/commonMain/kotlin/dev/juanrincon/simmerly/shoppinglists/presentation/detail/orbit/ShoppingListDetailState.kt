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
    /** Keys of the sections the user has collapsed. They stay collapsed as items change and
     * across switching grouping, since aisle and recipe keys never clash. */
    val collapsedSections: Set<String> = emptySet(),
    val checkedItems: List<ShoppingItemUi> = emptyList(),
    val checkedExpanded: Boolean = true,
    val addText: String = "",
    val isAdding: Boolean = false,
    /** Foods matching what's typed in the add-item bar. */
    val addSuggestions: List<FoodOptionUi> = emptyList(),
    /** The suggestion whose name matches exactly — what submitting the text adds. */
    val addHighlightedId: String? = null,
    /** The part of the typed text the suggestions matched, e.g. "milk" in "2 cups milk". */
    val addSuggestionQuery: String = "",
    val editor: ItemEditorUi? = null,
    val units: List<UnitOptionUi> = emptyList(),
    val foodSuggestions: List<FoodOptionUi> = emptyList(),
    val dialog: DetailDialog = DetailDialog.None,
) {
    /** Whether any section of the current grouping is open — "Collapse all" when it is,
     * "Expand all" once every one is collapsed. */
    val anySectionExpanded: Boolean
        get() = sections.any { it.key !in collapsedSections }
}

sealed interface DetailDialog {
    data object None : DetailDialog
    data class Rename(val currentName: String) : DetailDialog
    data object ConfirmDelete : DetailDialog
}
