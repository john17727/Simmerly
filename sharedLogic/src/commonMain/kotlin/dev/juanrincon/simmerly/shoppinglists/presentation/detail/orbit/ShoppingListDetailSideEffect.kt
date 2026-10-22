package dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit

import dev.juanrincon.simmerly.core.presentation.UiText

sealed interface ShoppingListDetailSideEffect {
    data object NavigateBack : ShoppingListDetailSideEffect
    data class ShowMessage(val message: UiText) : ShoppingListDetailSideEffect
}
