package dev.juanrincon.simmerly.recipes.presentation.search

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.recipes.aRecipeSummary
import dev.juanrincon.simmerly.recipes.presentation.search.orbit.RecipeSearchIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.test.test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeSearchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeRecipeRepository
    private lateinit var viewModel: RecipeSearchViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeRecipeRepository()
        viewModel = RecipeSearchViewModel(repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initial state

    @Test
    fun initialStateHasEmptyRecipesQueriesAndRecentItems() {
        val state = viewModel.container.stateFlow.value
        assertThat(state.recipes).isEmpty()
        assertThat(state.recentRecipes).isEmpty()
        assertThat(state.recentQueries).isEmpty()
        assertThat(state.searchQuery).isEqualTo("")
        assertThat(state.submittedQuery).isEqualTo("")
        assertThat(state.isLoading).isFalse()
    }

    // endregion

    // region Init flows

    @Test
    fun fetchRecipesEmitsLoadingThenRecipesInState() = runTest(testDispatcher) {
        viewModel.test(this) {
            runOnCreate()
            assertThat(awaitState().isLoading).isTrue()

            repo.allRecipesFlow.emit(listOf(aRecipeSummary()))
            val state = awaitState()
            assertThat(state.isLoading).isFalse()
            assertThat(state.recipes).hasSize(1)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun loadRecentlyViewedPopulatesRecentRecipesInState() = runTest(testDispatcher) {
        viewModel.test(this) {
            runOnCreate()
            repo.recentlyViewedFlow.emit(listOf(aRecipeSummary(id = "recent-1")))
            val state = awaitState()
            assertThat(state.recentRecipes).hasSize(1)
            assertThat(state.recentRecipes[0].id).isEqualTo("recent-1")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun loadRecentQueriesPopulatesRecentQueriesInState() = runTest(testDispatcher) {
        viewModel.test(this) {
            runOnCreate()
            repo.recentQueriesFlow.emit(listOf("pasta", "pizza"))
            val state = awaitState()
            assertThat(state.recentQueries).hasSize(2)
            assertThat(state.recentQueries[0]).isEqualTo("pasta")
            cancelAndIgnoreRemainingItems()
        }
    }

    // endregion

    // region Intents

    @Test
    fun onQueryChangedUpdatesSearchQuery() = runTest(testDispatcher) {
        viewModel.test(this) {
            viewModel.onEvent(RecipeSearchIntent.OnQueryChanged("pasta"))
            assertThat(awaitState().searchQuery).isEqualTo("pasta")
        }
    }

    @Test
    fun onQuerySubmittedUpdatesSubmittedQuery() = runTest(testDispatcher) {
        viewModel.test(this) {
            viewModel.onEvent(RecipeSearchIntent.OnQuerySubmitted("pasta"))
            assertThat(awaitState().submittedQuery).isEqualTo("pasta")
        }
    }

    @Test
    fun onQuerySubmittedRecordsQueryWhenNotBlank() = runTest(testDispatcher) {
        viewModel.test(this) {
            viewModel.onEvent(RecipeSearchIntent.OnQuerySubmitted("pasta"))
            awaitState()
        }
        assertThat(repo.recordedSearchQueries).contains("pasta")
    }

    @Test
    fun onQuerySubmittedDoesNotRecordBlankQuery() = runTest(testDispatcher) {
        viewModel.test(this) {
            viewModel.onEvent(RecipeSearchIntent.OnQuerySubmitted(""))
        }
        assertThat(repo.recordedSearchQueries).isEmpty()
    }

    @Test
    fun onRecipeViewedRecordsRecipeView() = runTest(testDispatcher) {
        viewModel.test(this) {
            viewModel.onEvent(RecipeSearchIntent.OnRecipeViewed("recipe-1"))
        }
        assertThat(repo.recordedRecipeViews).contains("recipe-1")
    }

    // endregion
}
