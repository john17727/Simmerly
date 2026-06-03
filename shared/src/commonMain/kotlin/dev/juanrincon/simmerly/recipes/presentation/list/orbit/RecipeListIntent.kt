package dev.juanrincon.simmerly.recipes.presentation.list.orbit

sealed interface RecipeListIntent {
    data object OnRefresh : RecipeListIntent
    data object OnLoadMore : RecipeListIntent
    data class OnRecipeSelected(val recipeId: String) : RecipeListIntent
    data class OnSearchQueryChanged(val query: String) : RecipeListIntent
}
