package dev.juanrincon.simmerly.mealplan.presentation.orbit

import dev.juanrincon.simmerly.mealplan.domain.MealType
import kotlinx.datetime.LocalDate

sealed interface MealPlanAddIntent {
    data class SetDate(val date: LocalDate) : MealPlanAddIntent
    data class SetWholeWeek(val wholeWeek: Boolean) : MealPlanAddIntent
    data class SetType(val type: MealType) : MealPlanAddIntent
    data class SetTab(val tab: AddTab) : MealPlanAddIntent
    data class SetQuery(val query: String) : MealPlanAddIntent
    data class ToggleFilter(val id: String) : MealPlanAddIntent
    data class SetRulesEnabled(val enabled: Boolean) : MealPlanAddIntent
    data class SelectRecipe(val recipeId: String) : MealPlanAddIntent
    data class ChangeServings(val delta: Int) : MealPlanAddIntent
    data class SetNoteTitle(val title: String) : MealPlanAddIntent
    data class SetNoteText(val text: String) : MealPlanAddIntent
    data object Save : MealPlanAddIntent
}
