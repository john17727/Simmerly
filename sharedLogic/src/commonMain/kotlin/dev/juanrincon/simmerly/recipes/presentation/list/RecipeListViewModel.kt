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
import kotlinx.coroutines.flow.stateIn
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

    // Concrete, non-generic accessors for Swift — see WelcomeViewModel for why these are needed.
    val stateFlow: StateFlow<RecipeListState> get() = container.stateFlow
    val sideEffectFlow: Flow<RecipeListSideEffect> get() = container.sideEffectFlow

    val recipes: Flow<PagingData<RecipeSummary>> = repository.recipeList()
        .cachedIn(viewModelScope)

    // Swift-friendly paging facade — PagingData/collectAsLazyPagingItems are Compose-only.
    private val _pagedRecipes = MutableStateFlow<List<RecipeSummary>>(emptyList())
    val pagedRecipes: StateFlow<List<RecipeSummary>> = _pagedRecipes.asStateFlow()

    /** Overlaid on the paged items: the paging source doesn't re-read preferences on change. */
    val favoriteIds: StateFlow<Set<String>> = repository.observeFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val _ratingOverrides = MutableStateFlow<Map<String, Double?>>(emptyMap())

    /** Ratings the user just set, shown over the paged items until the next list refresh. A `null`
     * value means the rating was cleared. */
    val ratingOverrides: StateFlow<Map<String, Double?>> = _ratingOverrides.asStateFlow()

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
            is RecipeListIntent.OnFavoriteToggled -> intent {
                repository.setFavorite(event.recipeId, event.favorite)
            }
            is RecipeListIntent.OnRatingChanged -> intent {
                val previous = _ratingOverrides.value
                _ratingOverrides.value = previous + (event.recipeId to event.rating)
                repository.setRating(event.recipeId, event.rating).onLeft {
                    // Roll back to whatever was shown before the tap.
                    _ratingOverrides.value = _ratingOverrides.value.let { current ->
                        if (event.recipeId in previous) current + (event.recipeId to previous[event.recipeId])
                        else current - event.recipeId
                    }
                }
            }
            is RecipeListIntent.OnSearchQueryChanged -> intent {
                reduce { state.copy(searchQuery = event.query) }
            }
        }
    }
}
