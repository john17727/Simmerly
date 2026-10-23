package dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit

import dev.juanrincon.simmerly.core.presentation.UiText

sealed interface ShoppingListsSideEffect {
    data class OpenList(val listId: String) : ShoppingListsSideEffect
    data class ShowMessage(val message: UiText) : ShoppingListsSideEffect
}
