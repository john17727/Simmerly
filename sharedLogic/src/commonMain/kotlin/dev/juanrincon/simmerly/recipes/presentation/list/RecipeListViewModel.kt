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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

    // Swift-friendly paging facade — PagingData/collectAsLazyPagingItems are Compose-only.
    private val _pagedRecipes = MutableStateFlow<List<RecipeSummary>>(emptyList())
    val pagedRecipes: StateFlow<List<RecipeSummary>> = _pagedRecipes.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _endReached = MutableStateFlow(false)
    val endReached: StateFlow<Boolean> = _endReached.asStateFlow()

    init {
        repository.observeAllRecipes()
            .onEach { _pagedRecipes.value = it }
            .launchIn(viewModelScope)
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || _endReached.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            repository.loadNextRecipePage().onRight { hasMore -> _endReached.value = !hasMore }
            _isLoadingMore.value = false
        }
    }

    fun refresh() {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            _endReached.value = false
            repository.refreshRecipeList().onRight { hasMore -> _endReached.value = !hasMore }
            _isLoadingMore.value = false
        }
    }

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
