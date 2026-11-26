package dev.juanrincon.simmerly.mealplan.presentation.orbit

import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.presentation.weekDays
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

data class MealPlanState(
    val today: LocalDate = LocalDate(1970, 1, 1),
    val selectedDate: LocalDate = today,
    val firstDay: DayOfWeek = DayOfWeek.MONDAY,
    /** Everything cached for the six-week month grid around [selectedDate]. */
    val entries: List<MealPlanEntry> = emptyList(),
    val isLoading: Boolean = true,
    /** Background refresh running (thin progress bar). */
    val isRefreshing: Boolean = false,
    val isPullRefreshing: Boolean = false,
    /** Showing the saved plan because the last refresh failed; edits are paused. */
    val isOffline: Boolean = false,
    /** Nothing cached and the load failed. */
    val hasError: Boolean = false,
) {
    val canEdit: Boolean get() = !isOffline

    val weekDays: List<LocalDate> get() = selectedDate.weekDays(firstDay)

    /** Whole-week ("any day") entries of the viewed week. */
    val anyDayEntries: List<MealPlanEntry>
        get() = entries.filter { it.isWholeWeek && it.coversDay(selectedDate) }

    /** One section per meal type for [selectedDate], in display order; empty ones included. */
    val daySections: List<DaySection>
        get() = MealType.entries.map { type ->
            DaySection(type, entries.filter { !it.isWholeWeek && it.date == selectedDate && it.type == type })
        }

    /** Days with something planned on them (the dots); any-day entries don't count. */
    val plannedDays: Set<LocalDate> get() = entries.filter { !it.isWholeWeek }.map { it.date }.toSet()

    /** Recipes (not notes) placed on [selectedDate]; what the day cart adds. */
    val dayRecipeEntries: List<MealPlanEntry>
        get() = entries.filter { !it.isWholeWeek && it.date == selectedDate && !it.isNote }

    /** Recipes placed on a day of the viewed week; what the week cart adds. */
    val weekRecipeEntries: List<MealPlanEntry>
        get() = entries.filter { !it.isWholeWeek && !it.isNote && it.date in weekDays }

    val isEmptyWeek: Boolean
        get() = anyDayEntries.isEmpty() && entries.none { it.date in weekDays && !it.isWholeWeek }
}

data class DaySection(val type: MealType, val entries: List<MealPlanEntry>)
