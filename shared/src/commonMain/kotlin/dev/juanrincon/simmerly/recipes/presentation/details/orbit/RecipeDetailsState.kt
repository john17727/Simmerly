package dev.juanrincon.simmerly.recipes.presentation.details.orbit

import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi

data class RecipeDetailsState(
    val loading: Boolean = true,
    val isRefreshing: Boolean = false,
    val recipe: RecipeDetailUi = RecipeDetailUi.emptyRecipe,
    val error: RecipesError? = null,
    val mobileTabs: List<RecipeTab> = emptyList(),
    val desktopTabs: List<RecipeTab> = emptyList(),
    val mode: RecipeMode = RecipeMode.READ_ONLY,
    val showSettings: Boolean = false,
)

enum class RecipeMode {
    READ_ONLY,
    EDIT
}
