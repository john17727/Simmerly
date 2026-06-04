package dev.juanrincon.simmerly.recipes.presentation.search.orbit

import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

data class RecipeSearchState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val submittedQuery: String = "",
    val recipes: List<RecipeSummary> = emptyList(),
    val recentRecipes: List<RecipeSummary> = emptyList(),
    val recentQueries: List<String> = emptyList()
)
