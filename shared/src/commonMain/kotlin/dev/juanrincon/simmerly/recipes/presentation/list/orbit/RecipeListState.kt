package dev.juanrincon.simmerly.recipes.presentation.list.orbit

import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

data class RecipeListState(
    val nextPage: Int? = null,
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val recipes: List<RecipeSummary> = emptyList(),
    val selectedRecipeId: String = ""
)
