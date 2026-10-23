package dev.juanrincon.simmerly.shoppinglists.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface ShoppingListDestinations : NavKey {
    @Serializable
    data object Lists : ShoppingListDestinations

    @Serializable
    data class Detail(val listId: String) : ShoppingListDestinations
}
