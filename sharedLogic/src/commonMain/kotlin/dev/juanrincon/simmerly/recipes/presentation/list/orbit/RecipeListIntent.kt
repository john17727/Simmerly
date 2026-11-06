package dev.juanrincon.simmerly.recipes.presentation.list.orbit

sealed interface RecipeListIntent {
    data class OnFavoriteToggled(val recipeId: String, val favorite: Boolean) : RecipeListIntent
    /** [rating] is a whole number of stars, or `null` to clear the user's rating. */
    data class OnRatingChanged(val recipeId: String, val rating: Double?) : RecipeListIntent
    data class OnSearchQueryChanged(val query: String) : RecipeListIntent
}
