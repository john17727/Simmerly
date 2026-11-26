package dev.juanrincon.simmerly.mealplan.presentation.orbit

import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter

sealed interface MealPlanCartIntent {
    data class SetScope(val scope: CartScope) : MealPlanCartIntent
    data class ToggleEntry(val entryId: Int) : MealPlanCartIntent
    data class SetCombine(val combine: Boolean) : MealPlanCartIntent
    data class ToggleRow(val key: String) : MealPlanCartIntent
    data object ToggleAll : MealPlanCartIntent
    data class SetFilter(val filter: ListFilter) : MealPlanCartIntent
    data class SelectList(val listId: String) : MealPlanCartIntent
    data object SelectNewList : MealPlanCartIntent
    data class SetNewListName(val name: String) : MealPlanCartIntent
    data object Confirm : MealPlanCartIntent
}
