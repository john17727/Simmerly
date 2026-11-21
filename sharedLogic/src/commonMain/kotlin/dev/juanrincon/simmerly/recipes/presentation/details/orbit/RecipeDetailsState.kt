package dev.juanrincon.simmerly.recipes.presentation.details.orbit

import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi

data class RecipeDetailsState(
    val loading: Boolean = true,
    /** A refresh the app started on its own (a stale copy was found); shown as a thin bar. */
    val isRefreshing: Boolean = false,
    /** A refresh the user asked for with pull-to-refresh; shown by the pull indicator. */
    val isPullRefreshing: Boolean = false,
    val recipe: RecipeDetailUi = RecipeDetailUi.emptyRecipe,
    val error: RecipesError? = null,
    val mobileTabs: List<RecipeTab> = emptyList(),
    val mode: RecipeMode = RecipeMode.READ_ONLY,
    val showSettings: Boolean = false,
    /** The recipe's own servings as loaded, before the reader scaled them. */
    val baseServings: Double = 0.0,
    val addToList: AddToListState? = null,
    val showDeleteConfirm: Boolean = false,
    val isDeleting: Boolean = false,
)

enum class RecipeMode {
    READ_ONLY,
    EDIT
}
