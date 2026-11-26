package dev.juanrincon.simmerly.mealplan.presentation.orbit

import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry

sealed interface MealPlanAddSideEffect {
    data class ShowMessage(val message: UiText) : MealPlanAddSideEffect

    /** Saved: close the screen; callers show "View plan" when opened from a recipe. */
    data class Saved(val entry: MealPlanEntry, val wasEdit: Boolean) : MealPlanAddSideEffect
}
