package dev.juanrincon.simmerly.recipes.presentation.search.orbit

sealed interface RecipeSearchIntent {
    data class OnQueryChanged(val query: String) : RecipeSearchIntent
    data class OnQuerySubmitted(val query: String) : RecipeSearchIntent
    data class OnRecipeViewed(val recipeId: String) : RecipeSearchIntent
}
