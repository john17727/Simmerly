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
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class RecipeListViewModel(
    private val repository: RecipeRepository,
) : ContainerHost<RecipeListState, RecipeListSideEffect>, ViewModel() {

    override val container: Container<RecipeListState, RecipeListSideEffect> =
        container(initialState = RecipeListState())

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
