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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

class RecipeListViewModel(
    private val repository: RecipeRepository,
) : OrbitContainerHost<RecipeListState, RecipeListState, RecipeListSideEffect>, ViewModel() {

    override val container: OrbitContainer<RecipeListState, RecipeListState, RecipeListSideEffect> =
        orbitContainer(initialState = RecipeListState())

    // Concrete, non-generic accessors for Swift — see WelcomeViewModel for why these are needed.
    val stateFlow: StateFlow<RecipeListState> get() = container.stateFlow
    val sideEffectFlow: Flow<RecipeListSideEffect> get() = container.sideEffectFlow

    // The repository caches the loaded pages (so they outlive this ViewModel) and merges in the
    // user's favorites and ratings; this shares that one merged stream with the UI.
    val recipes: Flow<PagingData<RecipeSummary>> = repository.recipeList()
        .cachedIn(viewModelScope)

    // Swift-friendly paging facade — PagingData/collectAsLazyPagingItems are Compose-only.
    // Only the iOS list reads this. Collected lazily, so Android/desktop never run the extra
    // query over every recipe (and its tag/category relations) that feeds it.
    val pagedRecipes: StateFlow<List<RecipeSummary>> = repository.observeAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _endReached = MutableStateFlow(false)
    val endReached: StateFlow<Boolean> = _endReached.asStateFlow()

    /**
     * The one function exposed besides [onEvent], and only for SwiftUI: `.refreshable` keeps its
     * spinner up until the awaited call returns, and an event is fire-and-forget so it can't
     * report when the refresh finished. Everything else, including Compose's refresh, goes
     * through [onEvent].
     */
    suspend fun refreshAndAwait() {
        if (_isLoadingMore.value) return
        _isLoadingMore.value = true
        _endReached.value = false
        repository.refreshRecipeList().onRight { hasMore -> _endReached.value = !hasMore }
        _isLoadingMore.value = false
    }

    private suspend fun loadNextPage() {
        if (_isLoadingMore.value || _endReached.value) return
        _isLoadingMore.value = true
        repository.loadNextRecipePage().onRight { hasMore -> _endReached.value = !hasMore }
        _isLoadingMore.value = false
    }

    fun onEvent(event: RecipeListIntent) {
        when (event) {
            is RecipeListIntent.OnFavoriteToggled -> intent {
                repository.setFavorite(event.recipeId, event.favorite)
            }
            is RecipeListIntent.OnRatingChanged -> intent {
                repository.setRating(event.recipeId, event.rating)
            }
            RecipeListIntent.OnLoadMore -> intent { loadNextPage() }
            RecipeListIntent.OnRefresh -> intent { refreshAndAwait() }
            is RecipeListIntent.OnSearchQueryChanged -> intent {
                reduce { state.copy(searchQuery = event.query) }
            }
        }
    }
}
