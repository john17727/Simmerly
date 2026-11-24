package dev.juanrincon.simmerly.timeline.presentation

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.timeline.domain.TimelineRepository
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineIntent
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineSideEffect
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TimelineViewModel(
    private val recipeId: String,
    private val repository: TimelineRepository,
    private val recipeRepository: RecipeRepository,
) : OrbitContainerHost<TimelineState, TimelineState, TimelineSideEffect>, ViewModel() {

    override val container: OrbitContainer<TimelineState, TimelineState, TimelineSideEffect> =
        orbitContainer(initialState = TimelineState()) {
            observeRecipe()
            observeTimeline()
        }

    // Concrete, non-generic accessors for Swift.
    val stateFlow: StateFlow<TimelineState> get() = container.stateFlow
    val sideEffectFlow: Flow<TimelineSideEffect> get() = container.sideEffectFlow

    fun onEvent(event: TimelineIntent) {
        when (event) {
            TimelineIntent.Refresh -> refresh()
            TimelineIntent.LoadMore -> loadMore()
            is TimelineIntent.ToggleType -> intent {
                reduce {
                    val types = if (event.type in state.visibleTypes) state.visibleTypes - event.type
                    else state.visibleTypes + event.type
                    state.copy(visibleTypes = types)
                }
            }

            TimelineIntent.ToggleSort -> intent { reduce { state.copy(newestFirst = !state.newestFirst) } }
            is TimelineIntent.DeleteClicked -> intent { reduce { state.copy(pendingDeleteId = event.eventId) } }
            TimelineIntent.DeleteDismissed -> intent { reduce { state.copy(pendingDeleteId = null) } }
            TimelineIntent.DeleteConfirmed -> delete()
        }
    }

    /** For SwiftUI's `.refreshable`; Compose sends [TimelineIntent.Refresh]. */
    suspend fun refreshAndAwait() {
        refresh().join()
    }

    private fun observeRecipe() = intent {
        recipeRepository.recipeDetails(recipeId).collect { result ->
            result.onRight { loading ->
                if (loading is LoadingResult.Loaded) {
                    reduce {
                        state.copy(
                            recipeName = loading.data.name,
                            lastMade = loading.data.lastMade?.let { formatShortDate(it) },
                        )
                    }
                }
            }
        }
    }

    private fun observeTimeline() = intent {
        repository.timeline(recipeId).collect { result ->
            result.fold(
                ifLeft = {
                    reduce { state.copy(isLoading = false, isRefreshing = false, hasError = state.events.isEmpty()) }
                    if (state.events.isNotEmpty()) {
                        postSideEffect(TimelineSideEffect.ShowMessage(UiText.Resource(StringKey.TimelineLoadFailed)))
                    }
                },
                ifRight = { loading ->
                    when (loading) {
                        LoadingResult.Loading -> reduce { state.copy(isLoading = true, hasError = false) }
                        LoadingResult.Refreshing -> reduce { state.copy(isRefreshing = true) }
                        LoadingResult.RefreshComplete -> reduce { state.copy(isRefreshing = false) }
                        is LoadingResult.Loaded -> reduce {
                            state.copy(
                                events = loading.data.map { it.toUi() },
                                isLoading = false,
                                hasError = false,
                            )
                        }
                    }
                }
            )
        }
    }

    private fun refresh() = intent {
        if (state.isPullRefreshing) return@intent
        reduce { state.copy(isPullRefreshing = true) }
        // New data arrives through observeTimeline; a failure only needs a message.
        repository.refresh(recipeId)
            .onRight { reduce { state.copy(canLoadMore = true, hasError = false) } }
            .onLeft {
                postSideEffect(TimelineSideEffect.ShowMessage(UiText.Resource(StringKey.TimelineLoadFailed)))
            }
        reduce { state.copy(isPullRefreshing = false) }
    }

    private fun loadMore() = intent {
        if (state.isLoadingMore || !state.canLoadMore || state.isLoading) return@intent
        reduce { state.copy(isLoadingMore = true) }
        repository.loadNextPage(recipeId).fold(
            ifLeft = {
                reduce { state.copy(isLoadingMore = false) }
                postSideEffect(TimelineSideEffect.ShowMessage(UiText.Resource(StringKey.TimelineLoadFailed)))
            },
            ifRight = { hasMore -> reduce { state.copy(isLoadingMore = false, canLoadMore = hasMore) } },
        )
    }

    private fun delete() = intent {
        val id = state.pendingDeleteId ?: return@intent
        if (state.isDeleting) return@intent
        reduce { state.copy(isDeleting = true) }
        repository.deleteEvent(recipeId, id).fold(
            ifLeft = {
                reduce { state.copy(isDeleting = false, pendingDeleteId = null) }
                postSideEffect(TimelineSideEffect.ShowMessage(UiText.Resource(StringKey.TimelineDeleteFailed)))
            },
            ifRight = { reduce { state.copy(isDeleting = false, pendingDeleteId = null) } },
        )
    }
}
