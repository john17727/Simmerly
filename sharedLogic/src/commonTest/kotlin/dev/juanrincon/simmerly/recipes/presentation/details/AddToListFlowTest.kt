package dev.juanrincon.simmerly.recipes.presentation.details

import arrow.core.Either
import arrow.core.left
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.initialload.FakeUserRepository
import dev.juanrincon.simmerly.recipes.aRecipeDetail
import dev.juanrincon.simmerly.recipes.aRecipeDetailUi
import dev.juanrincon.simmerly.recipes.anIngredient
import dev.juanrincon.simmerly.recipes.anIngredientUi
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListStep
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListTarget
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsSideEffect
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsState
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.shoppinglists.FakeShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.aRecipe
import dev.juanrincon.simmerly.shoppinglists.aShoppingList
import dev.juanrincon.simmerly.shoppinglists.awaitSideEffectSkippingStates
import dev.juanrincon.simmerly.shoppinglists.awaitStateMatching
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.test.OrbitScopedTestContextInternal
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

private typealias DetailsTestContext = OrbitScopedTestContextInternal<
    RecipeDetailsState, RecipeDetailsState, RecipeDetailsSideEffect, RecipeDetailsViewModel>

/** The recipe's "Add to shopping list" sheet: choosing a list, picking ingredients, adding them. */
@OptIn(ExperimentalCoroutinesApi::class)
class AddToListFlowTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var shoppingRepo: FakeShoppingListRepository
    private lateinit var recipeRepo: FakeRecipeRepository
    private lateinit var viewModel: RecipeDetailsViewModel

    private val pasta = anIngredient(referenceId = "i-1", display = "1 pound pasta", quantity = 1.0)
    private val salt = anIngredient(referenceId = "i-2", display = "salt", quantity = null, note = "to taste")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        shoppingRepo = FakeShoppingListRepository()
        shoppingRepo.lists.value = listOf(aShoppingList(id = "list-1", name = "Sunday brunch"))
        recipeRepo = FakeRecipeRepository()
        viewModel = RecipeDetailsViewModel(
            recipeId = "test-recipe",
            repository = recipeRepo,
            shoppingListRepository = shoppingRepo,
            userRepository = FakeUserRepository(),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** The sheet already on its ingredient step; [loadRecipe] supplies the recipe it is for. */
    private fun pickingState(
        selected: Set<Int> = setOf(0, 1),
        target: AddToListTarget = AddToListTarget.Existing("list-1", "Sunday brunch"),
        servings: Double = 4.0,
    ) = RecipeDetailsState(
        loading = false,
        addToList = AddToListState(
            step = AddToListStep.PickIngredients,
            target = target,
            servings = servings,
            selected = selected,
        ),
    )

    /** Loads the recipe the way the screen does, so the ViewModel holds its source ingredients. */
    private suspend fun DetailsTestContext.loadRecipe(ingredients: List<Ingredient> = listOf(pasta, salt)) {
        runOnCreate()
        recipeRepo.recipeDetailsFlow()
            .emit(Either.Right(LoadingResult.Loaded(aRecipeDetail(id = "test-recipe", ingredients = ingredients))))
        awaitStateMatching { it.recipe.ingredients.size == ingredients.size }
    }

    @Test
    fun openingTheSheetListsShoppingListsAndFlagsOnesWithThisRecipe() = runTest(testDispatcher) {
        shoppingRepo.lists.value = listOf(
            aShoppingList(id = "list-1", name = "Sunday brunch"),
            aShoppingList(id = "list-2", name = "Weeknights", recipes = listOf(aRecipe(id = "test-recipe"))),
        )
        val loaded = RecipeDetailsState(loading = false, recipe = aRecipeDetailUi(id = "test-recipe", ingredients = listOf(anIngredientUi())))
        viewModel.testWithInternalState(this, initialState = loaded) {
            viewModel.onEvent(RecipeDetailsIntent.OpenAddToList)
            val state = awaitStateMatching { it.addToList?.lists?.size == 2 }
            val rows = state.addToList!!.lists!!
            assertThat(rows.map { it.alreadyHasRecipe }).containsExactly(false, true)
            assertThat(state.addToList.selected).isEqualTo(setOf(0))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun addingEveryIngredientLinksTheWholeRecipe() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this, initialState = pickingState()) {
            loadRecipe()
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
            val effect = awaitSideEffectSkippingStates()
            assertThat(effect).isInstanceOf<RecipeDetailsSideEffect.ItemsAdded>()
            effect as RecipeDetailsSideEffect.ItemsAdded
            assertThat(effect.listId).isEqualTo("list-1")
            assertThat(effect.message)
                .isEqualTo(UiText.Resource(StringKey.ItemsAddedToList, listOf("2", "Sunday brunch")))
            cancelAndIgnoreRemainingItems()
        }
        assertThat(shoppingRepo.addedRecipes)
            .containsExactly(FakeShoppingListRepository.AddedRecipe("list-1", "test-recipe", 1.0, null))
    }

    @Test
    fun aPartialSelectionStillLinksTheRecipeWithJustTheTickedIngredients() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this, initialState = pickingState(selected = setOf(1))) {
            loadRecipe()
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
            val effect = awaitSideEffectSkippingStates()
            assertThat((effect as RecipeDetailsSideEffect.ItemsAdded).message)
                .isEqualTo(UiText.Resource(StringKey.ItemAddedToList, listOf("Sunday brunch")))
            cancelAndIgnoreRemainingItems()
        }
        assertThat(shoppingRepo.addedRecipes)
            .containsExactly(FakeShoppingListRepository.AddedRecipe("list-1", "test-recipe", 1.0, listOf(salt)))
    }

    @Test
    fun changedServingsScaleTheRecipeByAMultipleOfItsOwnServings() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this, initialState = pickingState(servings = 6.0)) {
            loadRecipe()
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
            awaitSideEffectSkippingStates()
            cancelAndIgnoreRemainingItems()
        }
        assertThat(shoppingRepo.addedRecipes.single().scale).isEqualTo(1.5)
    }

    @Test
    fun confirmingWithNothingSelectedDoesNothing() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this, initialState = pickingState(selected = emptySet())) {
            loadRecipe()
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
            cancelAndIgnoreRemainingItems()
        }
        assertThat(shoppingRepo.addedRecipes).isEmpty()
    }

    @Test
    fun aNewListIsCreatedBeforeTheRecipeIsAdded() = runTest(testDispatcher) {
        viewModel.testWithInternalState(
            this,
            initialState = pickingState(target = AddToListTarget.New("Weeknight dinners")),
        ) {
            loadRecipe()
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
            val effect = awaitSideEffectSkippingStates() as RecipeDetailsSideEffect.ItemsAdded
            assertThat(effect.listId).isEqualTo("new-list")
            assertThat(effect.message)
                .isEqualTo(UiText.Resource(StringKey.ListCreatedWithItems, listOf("Weeknight dinners", "2")))
            cancelAndIgnoreRemainingItems()
        }
        assertThat(shoppingRepo.createdNames).containsExactly("Weeknight dinners")
        assertThat(shoppingRepo.addedRecipes.single().listId).isEqualTo("new-list")
    }

    @Test
    fun aFailedAddKeepsTheSheetOpenAndShowsAMessage() = runTest(testDispatcher) {
        shoppingRepo.writeResult = ShoppingListError.UpdateError.left()
        viewModel.testWithInternalState(this, initialState = pickingState()) {
            loadRecipe()
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
            val effect = awaitSideEffectSkippingStates()
            assertThat(effect).isEqualTo(
                RecipeDetailsSideEffect.ShowMessage(UiText.Resource(StringKey.ShoppingListUpdateFailed))
            )
            cancelAndIgnoreRemainingItems()
        }
        val sheet = viewModel.container.stateFlow.value.addToList
        assertThat(sheet).isNotNull()
        assertThat(sheet!!.isSubmitting).isEqualTo(false)
    }

    @Test
    fun backFromIngredientsOfANewListReturnsToItsName() = runTest(testDispatcher) {
        viewModel.testWithInternalState(
            this,
            initialState = pickingState(target = AddToListTarget.New("Weeknight dinners")),
        ) {
            viewModel.onEvent(RecipeDetailsIntent.AddToListBack)
            assertThat(awaitStateMatching { true }.addToList!!.step).isEqualTo(AddToListStep.NewListName)
            cancelAndIgnoreRemainingItems()
        }
    }
}
