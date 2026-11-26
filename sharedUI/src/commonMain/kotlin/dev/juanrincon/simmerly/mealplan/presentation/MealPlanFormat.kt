package dev.juanrincon.simmerly.mealplan.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DinnerDining
import androidx.compose.material.icons.rounded.FreeBreakfast
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material.icons.rounded.RiceBowl
import androidx.compose.ui.graphics.vector.ImageVector
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.presentation.orbit.PlannedMeta
import kotlin.math.abs
import kotlin.math.roundToInt

fun MealType.label(): String = when (this) {
    MealType.Breakfast -> "Breakfast"
    MealType.Lunch -> "Lunch"
    MealType.Dinner -> "Dinner"
    MealType.Side -> "Side"
    MealType.Snack -> "Snack"
    MealType.Drink -> "Drink"
    MealType.Dessert -> "Dessert"
}

fun MealType.icon(): ImageVector = when (this) {
    MealType.Breakfast -> Icons.Rounded.FreeBreakfast
    MealType.Lunch -> Icons.Rounded.LunchDining
    MealType.Dinner -> Icons.Rounded.DinnerDining
    MealType.Side -> Icons.Rounded.RiceBowl
    MealType.Snack -> Icons.Rounded.Cookie
    MealType.Drink -> Icons.Rounded.LocalDrink
    MealType.Dessert -> Icons.Rounded.Cake
}

/** "4 servings" / "1 serving" / "2.5 servings". */
fun servingsLabel(servings: Double): String {
    val rounded = (servings * 10).roundToInt() / 10.0
    val text = if (abs(rounded - rounded.roundToInt()) < 0.001) rounded.roundToInt().toString() else rounded.toString()
    return if (rounded == 1.0) "1 serving" else "${text} servings"
}

fun PlannedMeta.label(): String = when (this) {
    PlannedMeta.NotPlanned -> "Not planned yet"
    PlannedMeta.ThisWeek -> "Planned this week"
    is PlannedMeta.On -> "Planned ${date.monthDayLabel()}"
}
