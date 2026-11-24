package dev.juanrincon.simmerly.timeline.presentation

import arrow.core.Either
import arrow.core.left
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.recipes.aRecipeDetail
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.shoppinglists.awaitSideEffectSkippingStates
import dev.juanrincon.simmerly.shoppinglists.awaitStateMatching
import dev.juanrincon.simmerly.timeline.FakeTimelineRepository
import dev.juanrincon.simmerly.timeline.aTimelineEvent
import dev.juanrincon.simmerly.timeline.domain.TimelineError
import dev.juanrincon.simmerly.timeline.domain.TimelineEventType
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineIntent
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineSideEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TimelineViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeTimelineRepository
    private lateinit var recipes: FakeRecipeRepository
    private lateinit var viewModel: TimelineViewModel

    private val events = listOf(
        aTimelineEvent("e3", TimelineEventType.Comment, timestamp = Instant.parse("2026-11-22T10:00:00Z")),
        aTimelineEvent("e2", TimelineEventType.Info, timestamp = Instant.parse("2026-11-21T10:00:00Z")),
        aTimelineEvent("e1", TimelineEventType.System, timestamp = Instant.parse("2026-11-20T10:00:00Z")),
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeTimelineRepository()
        recipes = FakeRecipeRepository()
        viewModel = TimelineViewModel("recipe-1", repo, recipes)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsLoadingWithEverythingVisibleNewestFirst() {
        val state = viewModel.container.stateFlow.value

        assertThat(state.isLoading).isTrue()
        assertThat(state.visibleTypes).isEqualTo(TimelineEventType.entries.toSet())
        assertThat(state.newestFirst).isTrue()
    }

    @Test
    fun loadedEventsReplaceTheLoadingState() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.timelineFlow.emit(Either.Right(LoadingResult.Loaded(events)))

            val state = awaitStateMatching { !it.isLoading }
            assertThat(state.events.map { it.id }).isEqualTo(listOf("e3", "e2", "e1"))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun theRecipeNameImageAndLastMadeComeFromTheRecipe() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            recipes.recipeDetailsFlow().emit(
                Either.Right(
                    LoadingResult.Loaded(
                        aRecipeDetail().copy(
                            name = "Carbonara",
                            image = "http://x/img",
                            lastMade = Instant.parse("2026-11-21T12:00:00Z"),
                        )
                    )
                )
            )

            val state = awaitStateMatching { it.recipeName.isNotEmpty() }
            assertThat(state.recipeName).isEqualTo("Carbonara")
            assertThat(state.recipeImage).isEqualTo("http://x/img")
            assertThat(state.lastMade == null).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun aFailedFirstLoadShowsTheErrorState() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.timelineFlow.emit(Either.Left(TimelineError.FetchError))

            assertThat(awaitStateMatching { it.hasError }.isLoading).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun aFailedRefreshOverCachedEventsKeepsThemAndShowsAMessage() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.timelineFlow.emit(Either.Right(LoadingResult.Loaded(events)))
            awaitStateMatching { it.events.isNotEmpty() }
            repo.timelineFlow.emit(Either.Left(TimelineError.FetchError))

            assertThat(awaitSideEffectSkippingStates() is TimelineSideEffect.ShowMessage).isTrue()
            assertThat(viewModel.container.stateFlow.value.events).hasSize(3)
            assertThat(viewModel.container.stateFlow.value.hasError).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun togglingATypeHidesAndShowsItsEvents() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.timelineFlow.emit(Either.Right(LoadingResult.Loaded(events)))
            awaitStateMatching { it.events.isNotEmpty() }

            viewModel.onEvent(TimelineIntent.ToggleType(TimelineEventType.System))
            val hidden = awaitStateMatching { TimelineEventType.System !in it.visibleTypes }
            assertThat(hidden.visibleEvents.map { it.id }).isEqualTo(listOf("e3", "e2"))

            viewModel.onEvent(TimelineIntent.ToggleType(TimelineEventType.System))
            assertThat(awaitStateMatching { TimelineEventType.System in it.visibleTypes }.visibleEvents).hasSize(3)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun sortingReversesTheOrderWithoutTouchingTheData() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.timelineFlow.emit(Either.Right(LoadingResult.Loaded(events)))
            awaitStateMatching { it.events.isNotEmpty() }

            viewModel.onEvent(TimelineIntent.ToggleSort)

            val state = awaitStateMatching { !it.newestFirst }
            assertThat(state.visibleEvents.map { it.id }).isEqualTo(listOf("e1", "e2", "e3"))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun refreshCallsTheRepositoryAndClearsThePullIndicator() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(TimelineIntent.Refresh)
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.refreshCount).isEqualTo(1)
        assertThat(viewModel.container.stateFlow.value.isPullRefreshing).isFalse()
    }

    @Test
    fun aFailedRefreshPostsAMessage() = runTest(dispatcher) {
        repo.refreshResult = TimelineError.FetchError.left()
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(TimelineIntent.Refresh)

            assertThat(awaitSideEffectSkippingStates() is TimelineSideEffect.ShowMessage).isTrue()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun loadMoreStopsAskingOnceTheEndIsReached() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.timelineFlow.emit(Either.Right(LoadingResult.Loaded(events)))
            awaitStateMatching { !it.isLoading }
            viewModel.onEvent(TimelineIntent.LoadMore)
            awaitStateMatching { !it.canLoadMore }
            viewModel.onEvent(TimelineIntent.LoadMore)
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.loadNextPageCount).isEqualTo(1)
    }

    @Test
    fun deleteNeedsConfirmationAndThenRemovesTheEntry() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(TimelineIntent.DeleteClicked("e2"))
            assertThat(awaitStateMatching { it.pendingDeleteId == "e2" }.isDeleting).isFalse()
            assertThat(repo.deletedIds).hasSize(0)

            viewModel.onEvent(TimelineIntent.DeleteConfirmed)

            assertThat(awaitStateMatching { it.pendingDeleteId == null && !it.isDeleting }.pendingDeleteId).isNull()
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.deletedIds).isEqualTo(listOf("e2"))
    }

    @Test
    fun dismissingTheConfirmationDeletesNothing() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(TimelineIntent.DeleteClicked("e2"))
            awaitStateMatching { it.pendingDeleteId == "e2" }
            viewModel.onEvent(TimelineIntent.DeleteDismissed)
            awaitStateMatching { it.pendingDeleteId == null }
        }
        assertThat(repo.deletedIds).hasSize(0)
    }

    @Test
    fun aFailedDeleteClosesTheDialogAndPostsAMessage() = runTest(dispatcher) {
        repo.deleteResult = TimelineError.DeleteError.left()
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(TimelineIntent.DeleteClicked("e2"))
            awaitStateMatching { it.pendingDeleteId == "e2" }
            viewModel.onEvent(TimelineIntent.DeleteConfirmed)

            assertThat(awaitSideEffectSkippingStates() is TimelineSideEffect.ShowMessage).isTrue()
            assertThat(viewModel.container.stateFlow.value.pendingDeleteId).isNull()
            cancelAndIgnoreRemainingItems()
        }
    }
}
