package dev.juanrincon.simmerly.recipes.presentation.search

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.search.orbit.RecipeSearchIntent
import dev.juanrincon.simmerly.recipes.presentation.search.orbit.RecipeSearchSideEffect
import dev.juanrincon.simmerly.recipes.presentation.search.orbit.RecipeSearchState
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

class RecipeSearchViewModel(
    private val repository: RecipeRepository,
) : OrbitContainerHost<RecipeSearchState, RecipeSearchState, RecipeSearchSideEffect>, ViewModel() {

    override val container: OrbitContainer<RecipeSearchState, RecipeSearchState, RecipeSearchSideEffect> =
        orbitContainer(initialState = RecipeSearchState()) {
            fetchRecipes()
            loadRecentlyViewed()
            loadRecentQueries()
        }

    fun onEvent(event: RecipeSearchIntent) {
        when (event) {
            is RecipeSearchIntent.OnQueryChanged -> intent {
                reduce { state.copy(searchQuery = event.query) }
            }
            is RecipeSearchIntent.OnQuerySubmitted -> intent {
                reduce { state.copy(submittedQuery = event.query) }
                if (event.query.isNotBlank()) {
                    repository.recordSearchQuery(event.query)
                }
            }
            is RecipeSearchIntent.OnRecipeViewed -> intent {
                repository.recordRecipeView(event.recipeId)
            }
        }
    }

    private fun fetchRecipes() = intent {
        reduce { state.copy(isLoading = true) }
        repository.observeAllRecipes().collect { recipes ->
            reduce { state.copy(recipes = recipes, isLoading = false) }
        }
    }

    private fun loadRecentlyViewed() = intent {
        repository.observeRecentlyViewed().collect { recipes ->
            reduce { state.copy(recentRecipes = recipes) }
        }
    }

    private fun loadRecentQueries() = intent {
        repository.observeRecentSearchQueries().collect { queries ->
            reduce { state.copy(recentQueries = queries) }
        }
    }
}
