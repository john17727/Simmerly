package dev.juanrincon.simmerly.mealplan.presentation.orbit

import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.domain.PlanRule
import dev.juanrincon.simmerly.mealplan.presentation.weekDays
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** How the add/edit flow was opened. [recipeId] set = from a recipe (no "What" picker). */
data class MealPlanAddArgs(
    val date: LocalDate,
    val type: MealType = MealType.Dinner,
    /** Set to edit an existing entry. */
    val entryId: Int? = null,
    val recipeId: String? = null,
)

enum class AddTab { Recipe, Note }

/** What the recipe list shows under a recipe's name. */
sealed interface PlannedMeta {
    data object NotPlanned : PlannedMeta
    data object ThisWeek : PlannedMeta
    data class On(val date: LocalDate) : PlannedMeta
}

data class PickerRecipe(val recipe: RecipeSummary, val meta: PlannedMeta)

data class FilterChipUi(val id: String, val name: String)

data class MealPlanAddState(
    val today: LocalDate = LocalDate(1970, 1, 1),
    val firstDay: DayOfWeek = DayOfWeek.MONDAY,
    /** The entry being edited (skipped by the duplicate check), or null when adding. */
    val editingId: Int? = null,
    /** Opened from a recipe: the recipe is fixed and the picker hidden. */
    val recipeLocked: Boolean = false,
    val isLoadingEntry: Boolean = false,
    val date: LocalDate = today,
    val wholeWeek: Boolean = false,
    val type: MealType = MealType.Dinner,
    val tab: AddTab = AddTab.Recipe,
    val query: String = "",
    val allRecipes: List<RecipeSummary> = emptyList(),
    val plannedDates: Map<String, List<LocalDate>> = emptyMap(),
    /** Entries around [date]'s week, for the duplicate warning, the any-day meta and calendar dots. */
    val weekEntries: List<MealPlanEntry> = emptyList(),
    val filters: List<FilterChipUi> = emptyList(),
    val selectedFilterIds: Set<String> = emptySet(),
    /** Planner rule for [date]/[type], if the household has one. */
    val rule: PlanRule? = null,
    val rulesEnabled: Boolean = false,
    val selectedRecipeId: String? = null,
    val baseServings: Double? = null,
    val servings: Double? = null,
    val noteTitle: String = "",
    val noteText: String = "",
    val isSaving: Boolean = false,
) {
    val rulesAvailable: Boolean get() = rule != null

    val recipes: List<PickerRecipe>
        get() {
            val week = date.weekDays(firstDay)
            val thisWeek = weekEntries.filter { it.isWholeWeek && it.coversDay(date) }
                .mapNotNull { it.recipe?.id }.toSet()
            return allRecipes
                .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
                .filter { r ->
                    selectedFilterIds.all { id -> r.tags.any { it.id == id } || r.categories.any { it.id == id } }
                }
                .map { r ->
                    val dates = plannedDates[r.id].orEmpty()
                    val meta = when {
                        r.id in thisWeek -> PlannedMeta.ThisWeek
                        dates.isEmpty() -> PlannedMeta.NotPlanned
                        else -> PlannedMeta.On(dates.firstOrNull { it >= today && it in week } ?: dates.firstOrNull { it >= today } ?: dates.last())
                    }
                    PickerRecipe(r, meta)
                }
        }

    /** The chosen recipe is already planned for this meal on the chosen day (or week). */
    val isDuplicate: Boolean
        get() {
            val id = selectedRecipeId ?: return false
            if (tab != AddTab.Recipe) return false
            val week = date.weekDays(firstDay)
            return weekEntries.any { e ->
                e.recipe?.id == id && e.type == type && editingId != e.id &&
                    if (wholeWeek) (e.isWholeWeek || e.date in week) else e.coversDay(date)
            }
        }

    val isEdit: Boolean get() = editingId != null

    val canSave: Boolean
        get() = !isSaving && !isLoadingEntry && when (tab) {
            AddTab.Recipe -> selectedRecipeId != null
            AddTab.Note -> noteTitle.isNotBlank()
        }
}
