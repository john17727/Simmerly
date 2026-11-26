package dev.juanrincon.simmerly.mealplan.presentation.orbit

import dev.juanrincon.simmerly.core.presentation.UiText

sealed interface MealPlanSideEffect {
    data class ShowMessage(val message: UiText) : MealPlanSideEffect

    /** A snackbar with an "Undo" action that sends [undo] back to the ViewModel. */
    data class ShowUndo(val message: UiText, val undo: MealPlanIntent) : MealPlanSideEffect
}
