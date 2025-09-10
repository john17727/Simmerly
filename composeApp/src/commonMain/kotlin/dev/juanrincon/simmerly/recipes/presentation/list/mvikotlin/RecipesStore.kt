package dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin

import androidx.paging.PagingData
import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore.Label
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface RecipesStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object OnRefresh : Intent
        data class OnRecipeClicked(val recipeId: String) : Intent
        data class OnSearchQueryChanged(val query: String) : Intent
    }

    data class State(
        val searchQuery: String = "",
        val recipes: Flow<PagingData<RecipeSummary>> = emptyFlow()
    )

    sealed interface Label {
        data class RecipeClicked(val recipeId: String) : Label
    }
}
