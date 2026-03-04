package dev.juanrincon.simmerly.navigation.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import dev.juanrincon.simmerly.core.presentation.UiIcon
import dev.juanrincon.simmerly.core.presentation.UiText
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.calendar_meal
import simmerly.composeapp.generated.resources.meal_plan
import simmerly.composeapp.generated.resources.profile
import simmerly.composeapp.generated.resources.recipes
import simmerly.composeapp.generated.resources.shopping_list

enum class AppDestinations(
    val label: UiText,
    val icon: UiIcon,
    val contentDescription: UiText
) {
    RECIPES(UiText.StringResText(Res.string.recipes), UiIcon.Vector(Icons.AutoMirrored.Default.MenuBook), UiText.StringResText(Res.string.recipes)),
    MEAL_PLAN(UiText.StringResText(Res.string.meal_plan), UiIcon.Drawable(Res.drawable.calendar_meal), UiText.StringResText(Res.string.meal_plan)),
    SHOPPING_LIST(UiText.StringResText(Res.string.shopping_list), UiIcon.Vector(Icons.AutoMirrored.Default.ListAlt), UiText.StringResText(Res.string.shopping_list)),
    PROFILE(UiText.StringResText(Res.string.profile), UiIcon.Vector(Icons.Default.AccountCircle), UiText.StringResText(Res.string.profile)),
}