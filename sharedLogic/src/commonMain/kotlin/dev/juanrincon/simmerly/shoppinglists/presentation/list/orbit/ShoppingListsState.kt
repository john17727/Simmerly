package dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit

import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.shoppinglists.presentation.list.models.ShoppingListCardUi

data class ShoppingListsState(
    /** Every list in the household; `null` until the first load. */
    val allLists: List<ShoppingListCardUi>? = null,
    val currentUserId: String? = null,
    val filter: ListFilter = ListFilter.Mine,
    val isSearching: Boolean = false,
    val query: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val dialog: ListDialog = ListDialog.None,
) {
    /** The lists to show under the current filter and search. "Mine" shows everything until the
     * signed-in user is known, rather than briefly showing nothing. */
    val lists: List<ShoppingListCardUi>
        get() = allLists.orEmpty()
            .filter { filter == ListFilter.Household || currentUserId == null || it.ownerId == currentUserId }
            .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
}

enum class ListFilter { Mine, Household }

sealed interface ListDialog {
    data object None : ListDialog
    data object Create : ListDialog
    data class Rename(val listId: String, val currentName: String) : ListDialog
    data class ConfirmDelete(val listId: String, val name: String) : ListDialog
}
