package dev.juanrincon.simmerly.recipes.presentation.details.orbit

import dev.juanrincon.simmerly.core.presentation.UiText

sealed interface RecipeDetailsSideEffect {
    data class ShowMessage(val message: UiText) : RecipeDetailsSideEffect

    /** The recipe was deleted; the screen should leave the now-gone recipe. */
    data object RecipeDeleted : RecipeDetailsSideEffect

    /** Items were added; the screen offers a "View list" action for [listId]. */
    data class ItemsAdded(val message: UiText, val listId: String) : RecipeDetailsSideEffect
}
