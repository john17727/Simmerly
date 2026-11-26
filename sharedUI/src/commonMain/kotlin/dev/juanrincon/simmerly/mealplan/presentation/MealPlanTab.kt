package dev.juanrincon.simmerly.mealplan.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.core.presentation.navigation.AppNavRequest

/** The Meal plan tab. A recipe tapped on a card opens in the Recipes tab. */
@Composable
fun MealPlanTab(
    onNavigate: (AppNavRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    MealPlanScreen(
        onOpenRecipe = { onNavigate(AppNavRequest.OpenRecipe(it)) },
        modifier = modifier,
    )
}
