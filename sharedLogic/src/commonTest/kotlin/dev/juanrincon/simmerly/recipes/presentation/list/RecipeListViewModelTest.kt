package dev.juanrincon.simmerly.recipes.presentation.list

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.recipes.aRecipeSummary
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.presentation.list.orbit.RecipeListIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeRecipeRepository
    private lateinit var viewModel: RecipeListViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeRecipeRepository()
        viewModel = RecipeListViewModel(repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Intents run on orbit's own dispatcher, so wait (in real time) for their effects. */
    private suspend fun awaitUntil(condition: () -> Boolean) = withContext(Dispatchers.Default) {
        withTimeout(3_000) { while (!condition()) delay(5) }
    }

    // region Initial state

    @Test
    fun initialStateHasEmptySearchQuery() {
        val state = viewModel.container.stateFlow.value
        assertThat(state.searchQuery).isEqualTo("")
    }

    // endregion

    // region Favorites

    @Test
    fun onFavoriteToggledForwardsToRepository() = runTest(testDispatcher) {
        viewModel.onEvent(RecipeListIntent.OnFavoriteToggled("recipe-7", true))
        awaitUntil { repo.lastSetFavoriteCall != null }
        assertThat(repo.lastSetFavoriteCall).isEqualTo("recipe-7" to true)
    }

    // endregion

    // region Ratings

    @Test
    fun onRatingChangedForwardsToRepository() = runTest(testDispatcher) {
        viewModel.onEvent(RecipeListIntent.OnRatingChanged("recipe-7", 4.0))
        awaitUntil { repo.lastSetRatingCall != null }
        assertThat(repo.lastSetRatingCall).isEqualTo("recipe-7" to 4.0)
    }

    // endregion

    // region OnSearchQueryChanged

    @Test
    fun onSearchQueryChangedUpdatesSearchQuery() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(RecipeListIntent.OnSearchQueryChanged("pasta"))
            assertThat(awaitInternalState().searchQuery).isEqualTo("pasta")
        }
    }

    // endregion

    // region Paging facade

    @Test
    fun pagedRecipesReflectsRepositoryObserveAllRecipes() = runTest(testDispatcher) {
        viewModel.pagedRecipes.test {
            assertThat(awaitItem()).isEqualTo(emptyList())
            val recipes = listOf(aRecipeSummary(id = "1"), aRecipeSummary(id = "2"))
            repo.allRecipesFlow.emit(recipes)
            assertThat(awaitItem()).isEqualTo(recipes)
        }
    }

    @Test
    fun loadNextPageSetsEndReachedFalseWhenMorePagesRemain() = runTest(testDispatcher) {
        repo.loadNextRecipePageResult = true.right()
        viewModel.loadNextPage()
        assertThat(viewModel.endReached.value).isFalse()
        assertThat(viewModel.isLoadingMore.value).isFalse()
        assertThat(repo.loadNextRecipePageCallCount).isEqualTo(1)
    }

    @Test
    fun loadNextPageSetsEndReachedTrueWhenNoMorePagesRemain() = runTest(testDispatcher) {
        repo.loadNextRecipePageResult = false.right()
        viewModel.loadNextPage()
        assertThat(viewModel.endReached.value).isTrue()
    }

    @Test
    fun loadNextPageIgnoredWhenAlreadyEndReached() = runTest(testDispatcher) {
        repo.loadNextRecipePageResult = false.right()
        viewModel.loadNextPage()
        assertThat(repo.loadNextRecipePageCallCount).isEqualTo(1)

        viewModel.loadNextPage()
        assertThat(repo.loadNextRecipePageCallCount).isEqualTo(1)
    }

    @Test
    fun loadNextPageLeavesEndReachedUnchangedOnError() = runTest(testDispatcher) {
        repo.loadNextRecipePageResult = RecipesError.FetchError.left()
        viewModel.loadNextPage()
        assertThat(viewModel.endReached.value).isFalse()
    }

    @Test
    fun refreshResetsEndReachedBeforeReloading() = runTest(testDispatcher) {
        repo.loadNextRecipePageResult = false.right()
        viewModel.loadNextPage()
        assertThat(viewModel.endReached.value).isTrue()

        repo.refreshRecipeListResult = true.right()
        viewModel.refresh()
        assertThat(viewModel.endReached.value).isFalse()
        assertThat(repo.refreshRecipeListCallCount).isEqualTo(1)
    }

    // endregion
}
