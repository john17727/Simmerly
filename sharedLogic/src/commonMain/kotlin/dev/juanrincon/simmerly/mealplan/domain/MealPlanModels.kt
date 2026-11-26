package dev.juanrincon.simmerly.mealplan.domain

import app.tracktion.core.domain.util.Error
import kotlinx.datetime.LocalDate

enum class MealType(val apiValue: String) {
    Breakfast("breakfast"), Lunch("lunch"), Dinner("dinner"),
    Side("side"), Snack("snack"), Drink("drink"), Dessert("dessert");

    /** Side, snack, drink and dessert share one "Sides and more" section/row. */
    val isMainMeal: Boolean get() = this == Breakfast || this == Lunch || this == Dinner

    companion object {
        fun fromApi(value: String): MealType = entries.firstOrNull { it.apiValue == value } ?: Dinner
    }
}

data class MealPlanEntry(
    val id: Int,
    val groupId: String,
    val userId: String,
    val date: LocalDate,
    /** Non-null for a whole-week entry ("any day this week"): [date]..[endDate]. */
    val endDate: LocalDate?,
    val type: MealType,
    val title: String,
    val text: String,
    val recipe: PlannedRecipe?,
    val recipeScale: Double,
) {
    val isWholeWeek: Boolean get() = endDate != null
    val isNote: Boolean get() = recipe == null

    /** Servings shown on the card: the recipe's own servings times the scale. */
    val servings: Double? get() = recipe?.let { it.servings * recipeScale }

    fun coversDay(day: LocalDate): Boolean = day >= date && day <= (endDate ?: date)
}

data class PlannedRecipe(
    val id: String,
    val name: String,
    val servings: Double,
    /** Thumbnail URL, or empty while the server host is unknown. */
    val image: String,
)

/** A new entry or an edit: [id] is null for a new one. */
data class MealPlanDraft(
    val date: LocalDate,
    val endDate: LocalDate? = null,
    val type: MealType,
    val title: String = "",
    val text: String = "",
    val recipeId: String? = null,
    val recipeScale: Double = 1.0,
)

/** A household planner rule: [day] null = every day. Applies [queryFilter] to recipe search. */
data class PlanRule(
    val day: kotlinx.datetime.DayOfWeek?,
    val type: MealType,
    val queryFilter: String,
)

sealed interface MealPlanError : Error {
    data object FetchError : MealPlanError
    data object SaveError : MealPlanError
    data object DeleteError : MealPlanError
}
