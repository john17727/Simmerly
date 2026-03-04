package dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

interface RecipeListStore : Store<RecipeListStore.Intent, RecipeListStore.State, RecipeListStore.Label> {

    sealed interface Intent {
        data object OnRefresh : Intent
        data object OnLoadMore : Intent
        data class OnRecipeSelected(val recipeId: String) : Intent
        data class OnSearchQueryChanged(val query: String) : Intent
    }

    data class State(
        val nextPage: Int? = null,
        val isLoading: Boolean = false,
        val searchQuery: String = "",
        val recipes: List<RecipeSummary> = emptyList(),
        val selectedRecipeId: String = ""
    )

    sealed interface Label {
    }
}
