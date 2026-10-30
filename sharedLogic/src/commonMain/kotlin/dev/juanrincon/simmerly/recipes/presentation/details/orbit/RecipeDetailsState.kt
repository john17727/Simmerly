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
    /** The recipe's own servings as loaded, before the reader scaled them. */
    val baseServings: Double = 0.0,
    val addToList: AddToListState? = null,
)

enum class RecipeMode {
    READ_ONLY,
    EDIT
}
