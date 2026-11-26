package dev.juanrincon.simmerly.mealplan.presentation.orbit

import dev.juanrincon.simmerly.core.presentation.UiText

sealed interface MealPlanCartSideEffect {
    data class ShowMessage(val message: UiText) : MealPlanCartSideEffect

    /** Added: close and show [message] with a "View list" action for [listId]. */
    data class Added(val message: UiText, val listId: String) : MealPlanCartSideEffect
}
