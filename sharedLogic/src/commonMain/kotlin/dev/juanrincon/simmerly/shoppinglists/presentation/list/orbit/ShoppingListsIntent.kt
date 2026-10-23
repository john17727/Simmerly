package dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit

sealed interface ShoppingListsIntent {
    data object OnRefresh : ShoppingListsIntent
    data class OnFilterSelected(val filter: ListFilter) : ShoppingListsIntent
    data object OnSearchToggled : ShoppingListsIntent
    data class OnQueryChanged(val query: String) : ShoppingListsIntent
    data class OnListClicked(val listId: String) : ShoppingListsIntent
    data object OnNewListClicked : ShoppingListsIntent
    data class OnRenameClicked(val listId: String) : ShoppingListsIntent
    data class OnDeleteClicked(val listId: String) : ShoppingListsIntent
    data object OnDialogDismissed : ShoppingListsIntent
    data class OnCreateConfirmed(val name: String) : ShoppingListsIntent
    data class OnRenameConfirmed(val name: String) : ShoppingListsIntent
    data object OnDeleteConfirmed : ShoppingListsIntent
}
