package dev.juanrincon.simmerly.shoppinglists.domain

import app.tracktion.core.domain.util.Error

sealed interface ShoppingListError : Error {
    data object FetchError : ShoppingListError
    data object UpdateError : ShoppingListError
    data object UnknownError : ShoppingListError
}
