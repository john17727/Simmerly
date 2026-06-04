package dev.juanrincon.simmerly.recipes.presentation.list

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
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
    fun initialStateHasEmptySearchQueryAndSelectedRecipeId() {
        val state = viewModel.container.stateFlow.value
        assertThat(state.searchQuery).isEqualTo("")
        assertThat(state.selectedRecipeId).isEqualTo("")
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
