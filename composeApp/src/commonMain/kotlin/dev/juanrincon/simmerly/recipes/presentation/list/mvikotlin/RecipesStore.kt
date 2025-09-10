package dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore.Label
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore.State

interface RecipesStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object OnRefresh : Intent
        data object OnLoadMore : Intent
        data class OnRecipeClicked(val recipeId: String) : Intent
        data class OnSearchQueryChanged(val query: String) : Intent
    }

    data class State(
        val nextPage: Int? = null,
        val isLoading: Boolean = false,
        val searchQuery: String = "",
        val recipes: List<RecipeSummary> = emptyList(),
    )

    sealed interface Label {
        data class RecipeClicked(val recipeId: String) : Label
    }
}
