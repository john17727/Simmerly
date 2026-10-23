package dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit

import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.FoodOptionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemGrouping

sealed interface ShoppingListDetailIntent {
    data object OnRefresh : ShoppingListDetailIntent
    data class OnGroupingSelected(val grouping: ItemGrouping) : ShoppingListDetailIntent
    data class OnItemCheckedChanged(val itemId: String, val checked: Boolean) : ShoppingListDetailIntent
    data object OnUncheckAllClicked : ShoppingListDetailIntent
    data object OnCheckAllClicked : ShoppingListDetailIntent
    data object OnUndoCheckAllClicked : ShoppingListDetailIntent
    data object OnDeleteCheckedClicked : ShoppingListDetailIntent
    data object OnCheckedExpandedToggled : ShoppingListDetailIntent
    data class OnSectionToggled(val sectionKey: String) : ShoppingListDetailIntent
    data object OnToggleAllSectionsClicked : ShoppingListDetailIntent
    data class OnAddTextChanged(val text: String) : ShoppingListDetailIntent
    data object OnAddItemSubmitted : ShoppingListDetailIntent
    data class OnAddSuggestionPicked(val food: FoodOptionUi) : ShoppingListDetailIntent
    data class OnRecipeQuantityChanged(val recipeId: String, val delta: Int) : ShoppingListDetailIntent

    data class OnEditItemClicked(val itemId: String) : ShoppingListDetailIntent
    data class OnEditorQuantityChanged(val quantity: String) : ShoppingListDetailIntent
    data class OnEditorUnitSelected(val unitId: String?) : ShoppingListDetailIntent
    data class OnEditorFoodQueryChanged(val query: String) : ShoppingListDetailIntent
    data class OnEditorFoodSelected(val food: FoodOptionUi?) : ShoppingListDetailIntent
    data class OnEditorNoteChanged(val note: String) : ShoppingListDetailIntent
    data object OnEditorSaveClicked : ShoppingListDetailIntent
    data object OnEditorDeleteClicked : ShoppingListDetailIntent
    data object OnEditorDismissed : ShoppingListDetailIntent

    data object OnRenameClicked : ShoppingListDetailIntent
    data object OnDeleteListClicked : ShoppingListDetailIntent
    data object OnDialogDismissed : ShoppingListDetailIntent
    data class OnRenameConfirmed(val name: String) : ShoppingListDetailIntent
    data object OnDeleteListConfirmed : ShoppingListDetailIntent
}
