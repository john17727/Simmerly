package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.presentation.list.orbit.RecipeListIntent
import dev.juanrincon.simmerly.recipes.presentation.list.orbit.RecipeListSideEffect
import dev.juanrincon.simmerly.recipes.presentation.list.orbit.RecipeListState
import kotlinx.coroutines.flow.Flow
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

class RecipeListViewModel(
    private val repository: RecipeRepository,
) : OrbitContainerHost<RecipeListState, RecipeListState, RecipeListSideEffect>, ViewModel() {

    override val container: OrbitContainer<RecipeListState, RecipeListState, RecipeListSideEffect> =
        orbitContainer(initialState = RecipeListState())

    val recipes: Flow<PagingData<RecipeSummary>> = repository.recipeList()
        .cachedIn(viewModelScope)

    fun onEvent(event: RecipeListIntent) {
        when (event) {
            is RecipeListIntent.OnRecipeSelected -> intent {
                reduce { state.copy(selectedRecipeId = event.recipeId) }
            }
            is RecipeListIntent.OnSearchQueryChanged -> intent {
                reduce { state.copy(searchQuery = event.query) }
            }
        }
    }
}
