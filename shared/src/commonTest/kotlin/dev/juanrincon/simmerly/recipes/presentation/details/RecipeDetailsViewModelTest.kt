package dev.juanrincon.simmerly.recipes.presentation.details

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.recipes.aRecipeDetail
import dev.juanrincon.simmerly.recipes.aRecipeDetailUi
import dev.juanrincon.simmerly.recipes.aSettings
import dev.juanrincon.simmerly.recipes.anIngredientUi
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsState
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class RecipeDetailsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeRecipeRepository
    private lateinit var viewModel: RecipeDetailsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeRecipeRepository()
        viewModel = RecipeDetailsViewModel(recipeId = "test-recipe", repository = repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initial state

    @Test
    fun initialStateHasEmptyRecipeAndLoadingTrue() {
        val state = viewModel.container.stateFlow.value
        assertThat(state.loading).isTrue()
        assertThat(state.recipe).isEqualTo(RecipeDetailUi.emptyRecipe)
        assertThat(state.error).isNull()
        assertThat(state.showSettings).isFalse()
    }

    // endregion

    // region Init flow

    @Test
    fun observeRecipeEmitsLoadingThenSetsRecipeInState() = runTest(testDispatcher) {
        // Start with loading=false so the Loading emission produces an observable state change
        viewModel.test(this, initialState = RecipeDetailsState(loading = false)) {
            runOnCreate()
            repo.recipeDetailsFlow().emit(Either.Right(LoadingResult.Loading))
            assertThat(awaitState().loading).isTrue()

            repo.recipeDetailsFlow()
                .emit(Either.Right(LoadingResult.Loaded(aRecipeDetail(id = "test-recipe"))))
            val state = awaitState()
            assertThat(state.loading).isFalse()
            assertThat(state.recipe.id).isEqualTo("test-recipe")
            assertThat(state.error).isNull()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun observeRecipeFetchErrorWhenNoRecipeLoadedSetsErrorState() = runTest(testDispatcher) {
        viewModel.test(this) {
            runOnCreate()
            repo.recipeDetailsFlow().emit(Either.Left(RecipesError.FetchError))
            val state = awaitState()
            assertThat(state.error).isEqualTo(RecipesError.FetchError)
            assertThat(state.loading).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    // endregion

    // region Serving updates

    @Test
    fun addServingIncrementsServingsAndScalesIngredients() = runTest(testDispatcher) {
        val loadedState = RecipeDetailsState(
            loading = false,
            recipe = aRecipeDetailUi(
                servings = 4.0,
                ingredients = listOf(anIngredientUi(quantity = 100.0))
            )
        )
        viewModel.test(this, initialState = loadedState) {
            viewModel.onEvent(RecipeDetailsIntent.AddServing)
            val state = awaitState()
            assertThat(state.recipe.servings).isEqualTo(5.0)
            assertThat(state.recipe.ingredients[0].quantity).isEqualTo(125.0)
        }
    }

    @Test
    fun removeServingDecrementsServings() = runTest(testDispatcher) {
        val loadedState = RecipeDetailsState(
            loading = false,
            recipe = aRecipeDetailUi(servings = 4.0)
        )
        viewModel.test(this, initialState = loadedState) {
            viewModel.onEvent(RecipeDetailsIntent.RemoveServing)
            assertThat(awaitState().recipe.servings).isEqualTo(3.0)
        }
    }

    @Test
    fun removeServingClampsAtOneAndEmitsNoStateChange() = runTest(testDispatcher) {
        val loadedState = RecipeDetailsState(
            loading = false,
            recipe = aRecipeDetailUi(servings = 1.0)
        )
        viewModel.test(this, initialState = loadedState) {
            viewModel.onEvent(RecipeDetailsIntent.RemoveServing)
            // clamped (1.0) == current (1.0) → early return, no reduce called
        }
        assertThat(viewModel.container.stateFlow.value.recipe.servings).isEqualTo(1.0)
    }

    // endregion

    // region Settings UI

    @Test
    fun showSettingsSetsShowSettingsTrue() = runTest(testDispatcher) {
        val loadedState = RecipeDetailsState(loading = false, recipe = aRecipeDetailUi())
        viewModel.test(this, initialState = loadedState) {
            viewModel.onEvent(RecipeDetailsIntent.ShowSettings)
            assertThat(awaitState().showSettings).isTrue()
        }
    }

    @Test
    fun dismissSettingsSetsShowSettingsFalse() = runTest(testDispatcher) {
        val loadedState = RecipeDetailsState(
            loading = false,
            recipe = aRecipeDetailUi(),
            showSettings = true
        )
        viewModel.test(this, initialState = loadedState) {
            viewModel.onEvent(RecipeDetailsIntent.DismissSettings)
            assertThat(awaitState().showSettings).isFalse()
        }
    }

    // endregion

    // region UpdateSettings

    @Test
    fun updateSettingsOptimisticallyUpdatesRecipeSettingsInState() = runTest(testDispatcher) {
        val newSettings = aSettings(showNutrition = true, disableComments = false)
        val loadedState = RecipeDetailsState(loading = false, recipe = aRecipeDetailUi())
        viewModel.test(this, initialState = loadedState) {
            viewModel.onEvent(RecipeDetailsIntent.UpdateSettings(newSettings))
            assertThat(awaitState().recipe.settings).isEqualTo(newSettings)
        }
    }

    @Test
    fun updateSettingsCallsRepository() = runTest(testDispatcher) {
        val newSettings = aSettings(showNutrition = true)
        val loadedState = RecipeDetailsState(
            loading = false,
            recipe = aRecipeDetailUi(id = "test-recipe")
        )
        viewModel.test(this, initialState = loadedState) {
            viewModel.onEvent(RecipeDetailsIntent.UpdateSettings(newSettings))
            awaitState() // optimistic update
        }
        assertThat(repo.lastUpdateSettingsCall?.first).isEqualTo("test-recipe")
        assertThat(repo.lastUpdateSettingsCall?.second).isEqualTo(newSettings)
    }

    // endregion
}
