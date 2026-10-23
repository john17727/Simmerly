package dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit

import dev.juanrincon.simmerly.core.presentation.UiText

sealed interface ShoppingListDetailSideEffect {
    data object NavigateBack : ShoppingListDetailSideEffect
    data class ShowMessage(val message: UiText) : ShoppingListDetailSideEffect

    /** "N items checked" with an Undo action, after Check all. */
    data class ShowCheckedAll(val count: Int) : ShoppingListDetailSideEffect
}
