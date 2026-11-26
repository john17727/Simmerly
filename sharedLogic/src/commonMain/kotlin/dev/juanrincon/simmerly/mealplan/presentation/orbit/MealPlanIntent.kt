package dev.juanrincon.simmerly.mealplan.presentation.orbit

import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import kotlinx.datetime.LocalDate

sealed interface MealPlanIntent {
    data class SelectDate(val date: LocalDate) : MealPlanIntent
    data object GoToToday : MealPlanIntent

    /** [weeks] is -1 or +1; keeps the same weekday. */
    data class StepWeeks(val weeks: Int) : MealPlanIntent
    data object Refresh : MealPlanIntent

    /** Servings stepper: [delta] servings (usually +/-1). */
    data class ChangeServings(val entryId: Int, val delta: Int) : MealPlanIntent

    /** "Cook today": moves an any-day entry onto the selected day. */
    data class CookOnSelectedDay(val entryId: Int) : MealPlanIntent
    data class Remove(val entryId: Int) : MealPlanIntent

    /** Undo actions carry the entry as it was. */
    data class UndoRemove(val entry: MealPlanEntry) : MealPlanIntent
    data class UndoMove(val entry: MealPlanEntry) : MealPlanIntent
}
