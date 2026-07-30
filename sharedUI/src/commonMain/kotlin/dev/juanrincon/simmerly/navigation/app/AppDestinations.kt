package dev.juanrincon.simmerly.navigation.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiIcon
import dev.juanrincon.simmerly.core.presentation.UiText
import simmerly.shared.generated.resources.Res
import simmerly.shared.generated.resources.calendar_meal

enum class AppDestinations(
    val label: UiText,
    val icon: UiIcon,
    val contentDescription: UiText
) {
    RECIPES(
        UiText.Resource(StringKey.Recipes),
        UiIcon.Vector(Icons.AutoMirrored.Default.MenuBook),
        UiText.Resource(StringKey.Recipes)
    ),
    MEAL_PLAN(
        UiText.Resource(StringKey.MealPlan),
        UiIcon.Drawable(Res.drawable.calendar_meal),
        UiText.Resource(StringKey.MealPlan)
    ),
    SHOPPING_LIST(
        UiText.Resource(StringKey.ShoppingList),
        UiIcon.Vector(Icons.AutoMirrored.Default.ListAlt),
        UiText.Resource(StringKey.ShoppingList)
    ),
    PROFILE(
        UiText.Resource(StringKey.Profile),
        UiIcon.Vector(Icons.Default.AccountCircle),
        UiText.Resource(StringKey.Profile)
    ),
}