package dev.juanrincon.simmerly.recipes.presentation.details

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.initialload.FakeUserRepository
import dev.juanrincon.simmerly.recipes.aRecipeDetailUi
import dev.juanrincon.simmerly.recipes.anIngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.FoodUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListStep
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListTarget
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsSideEffect
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsState
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.shoppinglists.FakeShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.aFood
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
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/** The recipe's "Add to shopping list" sheet: choosing a list, picking ingredients, adding them. */
@OptIn(ExperimentalCoroutinesApi::class)
class AddToListFlowTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var shoppingRepo: FakeShoppingListRepository
    private lateinit var viewModel: RecipeDetailsViewModel

    private val pasta = IngredientUi(
        referenceId = "i-1", quantity = null, display = "1 pound pasta", food = null, unit = null, note = null,
    )
    private val salt = IngredientUi(
        referenceId = "i-2", quantity = null, display = "salt", food = null, unit = null, note = "to taste",
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        shoppingRepo = FakeShoppingListRepository()
        shoppingRepo.lists.value = listOf(aShoppingList(id = "list-1", name = "Sunday brunch"))
        viewModel = RecipeDetailsViewModel(
            recipeId = "test-recipe",
            repository = FakeRecipeRepository(),
            shoppingListRepository = shoppingRepo,
            userRepository = FakeUserRepository(),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** A loaded recipe with the sheet already on its ingredient step. */
    private fun pickingState(
        ingredients: List<IngredientUi> = listOf(pasta, salt),
        selected: Set<Int> = ingredients.indices.toSet(),
        target: AddToListTarget = AddToListTarget.Existing("list-1", "Sunday brunch"),
        servings: Double = 4.0,
    ) = RecipeDetailsState(
        loading = false,
        recipe = aRecipeDetailUi(id = "test-recipe", servings = 4.0, ingredients = ingredients),
        baseServings = 4.0,
        addToList = AddToListState(
            step = AddToListStep.PickIngredients,
            target = target,
            servings = servings,
            selected = selected,
        ),
    )

    @Test
    fun openingTheSheetListsShoppingListsAndFlagsOnesWithThisRecipe() = runTest(testDispatcher) {
        shoppingRepo.lists.value = listOf(
            aShoppingList(id = "list-1", name = "Sunday brunch"),
            aShoppingList(id = "list-2", name = "Weeknights", recipes = listOf(aRecipe(id = "test-recipe"))),
        )
        val loaded = RecipeDetailsState(loading = false, recipe = aRecipeDetailUi(id = "test-recipe", ingredients = listOf(pasta)))
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
    fun addingEveryIngredientAtTheRecipesServingsLinksTheRecipe() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this, initialState = pickingState()) {
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
            val effect = awaitSideEffectSkippingStates()
            assertThat(effect).isInstanceOf<RecipeDetailsSideEffect.ItemsAdded>()
            effect as RecipeDetailsSideEffect.ItemsAdded
            assertThat(effect.listId).isEqualTo("list-1")
            assertThat(effect.message)
                .isEqualTo(UiText.Resource(StringKey.ItemsAddedToList, listOf("2", "Sunday brunch")))
            cancelAndIgnoreRemainingItems()
        }
        assertThat(shoppingRepo.recipeQuantityCalls).containsExactly(Triple("list-1", "test-recipe", 1))
        assertThat(shoppingRepo.addedItems).isEmpty()
    }

    @Test
    fun aPartialSelectionAddsEachTickedIngredientAsNoteText() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this, initialState = pickingState(selected = setOf(1))) {
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
            val effect = awaitSideEffectSkippingStates()
            assertThat((effect as RecipeDetailsSideEffect.ItemsAdded).message)
                .isEqualTo(UiText.Resource(StringKey.ItemAddedToList, listOf("Sunday brunch")))
            cancelAndIgnoreRemainingItems()
        }
        assertThat(shoppingRepo.recipeQuantityCalls).isEmpty()
        assertThat(shoppingRepo.addedItems).containsExactly("list-1" to "salt, to taste")
    }

    @Test
    fun changedServingsScaleAmountsOfIngredientsMatchedToAMealieFood() = runTest(testDispatcher) {
        val milk = aFood(id = "milk", name = "milk")
        shoppingRepo.foodsResult = listOf(milk).right()
        val parsedMilk = anIngredientUi(quantity = 100.0).copy(food = FoodUi(name = "milk", pluralName = null))
        viewModel.testWithInternalState(
            this,
            initialState = pickingState(ingredients = listOf(parsedMilk), servings = 8.0),
        ) {
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
            awaitSideEffectSkippingStates()
            cancelAndIgnoreRemainingItems()
        }
        assertThat(shoppingRepo.recipeQuantityCalls).isEmpty()
        assertThat(shoppingRepo.addedFoodItems.map { it.food to it.quantity }).containsExactly(milk to 200.0)
    }

    @Test
    fun aFoodMealieDoesNotKnowIsAddedAsNoteText() = runTest(testDispatcher) {
        val parsedMilk = anIngredientUi(quantity = 100.0).copy(food = FoodUi(name = "milk", pluralName = null))
        viewModel.testWithInternalState(
            this,
            initialState = pickingState(ingredients = listOf(parsedMilk, salt), selected = setOf(0)),
        ) {
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
            awaitSideEffectSkippingStates()
            cancelAndIgnoreRemainingItems()
        }
        assertThat(shoppingRepo.addedFoodItems).isEmpty()
        assertThat(shoppingRepo.addedItems).containsExactly("list-1" to "100 Milk")
    }

    @Test
    fun confirmingWithNothingSelectedDoesNothing() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this, initialState = pickingState(selected = emptySet())) {
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
        }
        assertThat(shoppingRepo.recipeQuantityCalls).isEmpty()
        assertThat(shoppingRepo.addedItems).isEmpty()
    }

    @Test
    fun aNewListIsCreatedBeforeItsItemsAreAdded() = runTest(testDispatcher) {
        viewModel.testWithInternalState(
            this,
            initialState = pickingState(target = AddToListTarget.New("Weeknight dinners")),
        ) {
            viewModel.onEvent(RecipeDetailsIntent.AddToListConfirm)
            val effect = awaitSideEffectSkippingStates() as RecipeDetailsSideEffect.ItemsAdded
            assertThat(effect.listId).isEqualTo("new-list")
            assertThat(effect.message)
                .isEqualTo(UiText.Resource(StringKey.ListCreatedWithItems, listOf("Weeknight dinners", "2")))
            cancelAndIgnoreRemainingItems()
        }
        assertThat(shoppingRepo.createdNames).containsExactly("Weeknight dinners")
        assertThat(shoppingRepo.recipeQuantityCalls).containsExactly(Triple("new-list", "test-recipe", 1))
    }

    @Test
    fun aFailedAddKeepsTheSheetOpenAndShowsAMessage() = runTest(testDispatcher) {
        shoppingRepo.writeResult = ShoppingListError.UpdateError.left()
        viewModel.testWithInternalState(this, initialState = pickingState()) {
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
