package dev.juanrincon.simmerly.recipes.presentation.list

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.recipes.aRecipeListResult
import dev.juanrincon.simmerly.recipes.aRecipeSummary
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.presentation.list.orbit.RecipeListIntent
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

    // region Initial state

    @Test
    fun initialStateHasEmptyRecipesNoNextPageAndIsLoadingFalse() {
        val state = viewModel.container.stateFlow.value
        assertThat(state.recipes).hasSize(0)
        assertThat(state.nextPage).isEqualTo(null)
        assertThat(state.isLoading).isFalse()
        assertThat(state.searchQuery).isEqualTo("")
        assertThat(state.selectedRecipeId).isEqualTo("")
    }

    // endregion

    // region Init fetch

    @Test
    fun initEmitsLoadingThenRecipesAreLoadedIntoState() = runTest(testDispatcher) {
        viewModel.test(this) {
            runOnCreate()
            repo.recipeListFlowAt(0).emit(Either.Right(LoadingResult.Loading))
            assertThat(awaitState().isLoading).isTrue()

            repo.recipeListFlowAt(0)
                .emit(Either.Right(LoadingResult.Loaded(aRecipeListResult(aRecipeSummary()))))
            val state = awaitState()
            assertThat(state.isLoading).isFalse()
            assertThat(state.recipes).hasSize(1)
            assertThat(state.recipes[0].id).isEqualTo("recipe-1")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun initFetchErrorSetsIsLoadingFalse() = runTest(testDispatcher) {
        viewModel.test(this) {
            runOnCreate()
            repo.recipeListFlowAt(0).emit(Either.Right(LoadingResult.Loading))
            assertThat(awaitState().isLoading).isTrue()

            repo.recipeListFlowAt(0).emit(Either.Left(RecipesError.FetchError))
            assertThat(awaitState().isLoading).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    // endregion

    // region OnRefresh

    @Test
    fun onRefreshStartsNewFetchWhenNotLoading() = runTest(testDispatcher) {
        viewModel.test(this) {
            runOnCreate()
            // Let init complete with one recipe
            repo.recipeListFlowAt(0)
                .emit(Either.Right(LoadingResult.Loaded(aRecipeListResult(aRecipeSummary()))))
            awaitState() // isLoading = false, recipes = [recipe-1]

            viewModel.onEvent(RecipeListIntent.OnRefresh)
            // Second call to recipeList created a new flow at index 1; use a different id so state changes
            repo.recipeListFlowAt(1)
                .emit(Either.Right(LoadingResult.Loaded(aRecipeListResult(aRecipeSummary(id = "recipe-2")))))
            val state = awaitState()
            assertThat(state.recipes).hasSize(1)
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.recipeListCallCount).isEqualTo(2)
    }

    @Test
    fun onRefreshDoesNothingWhenAlreadyLoading() = runTest(testDispatcher) {
        viewModel.test(this) {
            runOnCreate()
            repo.recipeListFlowAt(0).emit(Either.Right(LoadingResult.Loading))
            awaitState() // isLoading = true

            viewModel.onEvent(RecipeListIntent.OnRefresh)
            // Guard fires: isLoading == true → return, no new fetch
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.recipeListCallCount).isEqualTo(1)
    }

    // endregion

    // region OnLoadMore

    @Test
    fun onLoadMoreFetchesNextPageWhenNextPageExists() = runTest(testDispatcher) {
        viewModel.test(this) {
            runOnCreate()
            repo.recipeListFlowAt(0).emit(
                Either.Right(
                    LoadingResult.Loaded(
                        aRecipeListResult(
                            aRecipeSummary(),
                            next = "page2"
                        )
                    )
                )
            )
            awaitState() // recipes loaded, nextPage = "page2"

            viewModel.onEvent(RecipeListIntent.OnLoadMore)
            // fetchAndObserve(next = "page2") created recipeListFlowAt(1)
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.recipeListCallCount).isEqualTo(2)
        assertThat(repo.lastRecipeListCall?.first).isEqualTo("page2")
    }

    @Test
    fun onLoadMoreDoesNothingWhenNextPageIsNull() = runTest(testDispatcher) {
        viewModel.test(this) {
            runOnCreate()
            viewModel.onEvent(RecipeListIntent.OnLoadMore)
            // nextPage is null in initial state → return, no fetch
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.recipeListCallCount).isEqualTo(1) // only init
    }

    // endregion

    // region OnRecipeSelected / OnSearchQueryChanged

    @Test
    fun onRecipeSelectedUpdatesSelectedRecipeId() = runTest(testDispatcher) {
        viewModel.test(this) {
            viewModel.onEvent(RecipeListIntent.OnRecipeSelected("recipe-42"))
            assertThat(awaitState().selectedRecipeId).isEqualTo("recipe-42")
        }
    }

    @Test
    fun onSearchQueryChangedUpdatesSearchQuery() = runTest(testDispatcher) {
        viewModel.test(this) {
            viewModel.onEvent(RecipeListIntent.OnSearchQueryChanged("pasta"))
            assertThat(awaitState().searchQuery).isEqualTo("pasta")
        }
    }

    // endregion
}
