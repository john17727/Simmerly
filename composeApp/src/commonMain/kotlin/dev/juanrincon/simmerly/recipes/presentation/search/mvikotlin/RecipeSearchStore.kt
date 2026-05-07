package dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

interface RecipeSearchStore :
    Store<RecipeSearchStore.Intent, RecipeSearchStore.State, RecipeSearchStore.Label> {

    sealed interface Intent {
        data class OnQueryChanged(val query: String) : Intent
        data class OnQuerySubmitted(val query: String) : Intent
    }

    data class State(
        val isLoading: Boolean = false,
        val searchQuery: String = "",
        val submittedQuery: String = "",
        val recipes: List<RecipeSummary> = emptyList()
    )

    sealed interface Label
}
