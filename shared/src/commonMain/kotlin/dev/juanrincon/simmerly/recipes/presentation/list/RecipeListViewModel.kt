package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.list.orbit.RecipeListIntent
import dev.juanrincon.simmerly.recipes.presentation.list.orbit.RecipeListSideEffect
import dev.juanrincon.simmerly.recipes.presentation.list.orbit.RecipeListState
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class RecipeListViewModel(
    private val repository: RecipeRepository,
) : ContainerHost<RecipeListState, RecipeListSideEffect>, ViewModel() {

    override val container: Container<RecipeListState, RecipeListSideEffect> =
        container(initialState = RecipeListState()) {
            fetchAndObserve(page = 1, refresh = true)
        }

    fun onEvent(event: RecipeListIntent) {
        when (event) {
            RecipeListIntent.OnRefresh -> {
                if (container.stateFlow.value.isLoading) return
                fetchAndObserve(page = 1, refresh = true)
            }
            RecipeListIntent.OnLoadMore -> {
                val nextPage = container.stateFlow.value.nextPage ?: return
                fetchAndObserve(page = nextPage)
            }
            is RecipeListIntent.OnRecipeSelected -> intent {
                reduce { state.copy(selectedRecipeId = event.recipeId) }
            }
            is RecipeListIntent.OnSearchQueryChanged -> intent {
                reduce { state.copy(searchQuery = event.query) }
            }
        }
    }

    private fun fetchAndObserve(page: Int, refresh: Boolean = false) = intent {
        repository.recipeList(page = page, refresh = refresh).collect { response ->
            response.fold(
                ifLeft = {
                    reduce { state.copy(isLoading = false) }
                },
                ifRight = { result ->
                    when (result) {
                        is LoadingResult.Loading -> reduce { state.copy(isLoading = true) }
                        is LoadingResult.Loaded -> reduce {
                            state.copy(
                                recipes = result.data.items,
                                nextPage = result.data.pagination?.next,
                                isLoading = false
                            )
                        }
                        else -> {}
                    }
                }
            )
        }
    }
}
