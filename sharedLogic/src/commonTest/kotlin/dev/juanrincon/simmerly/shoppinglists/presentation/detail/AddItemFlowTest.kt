package dev.juanrincon.simmerly.shoppinglists.presentation.detail

import arrow.core.right
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.shoppinglists.FakeShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.FakeShoppingListRepository.AddedFoodItem
import dev.juanrincon.simmerly.shoppinglists.aFood
import dev.juanrincon.simmerly.shoppinglists.aShoppingList
import dev.juanrincon.simmerly.shoppinglists.aUnit
import dev.juanrincon.simmerly.shoppinglists.awaitStateMatching
import dev.juanrincon.simmerly.shoppinglists.domain.model.ParsedIngredient
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.FoodOptionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailIntent
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailSideEffect
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailState
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

private typealias DetailTestContext = OrbitScopedTestContextInternal<
    ShoppingListDetailState, ShoppingListDetailState, ShoppingListDetailSideEffect, ShoppingListDetailViewModel>

/** The add-item bar: suggestions while typing, then exact match → parser → plain note. */
@OptIn(ExperimentalCoroutinesApi::class)
class AddItemFlowTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeShoppingListRepository
    private lateinit var viewModel: ShoppingListDetailViewModel

    private val milk = aFood(id = "milk", name = "milk", labelId = "dairy")
    private val cup = aUnit(id = "cup", name = "cup", pluralName = "cups")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeShoppingListRepository()
        repo.lists.value = listOf(aShoppingList())
        repo.unitsResult = listOf(cup).right()
        viewModel = ShoppingListDetailViewModel("list-1", repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun parsed(
        quantity: Double? = null,
        unit: Boolean = false,
        unitName: String? = null,
        food: Boolean = false,
        foodName: String? = null,
        note: String = "",
    ) = ParsedIngredient(
        quantity = quantity,
        unit = if (unit) cup else null,
        unitName = unitName,
        food = if (food) milk else null,
        foodName = foodName,
        note = note,
    )

    private suspend fun DetailTestContext.type(
        text: String,
        expectSuggestions: Boolean,
    ) {
        viewModel.onEvent(ShoppingListDetailIntent.OnAddTextChanged(text))
        if (expectSuggestions) awaitStateMatching { it.addSuggestions.isNotEmpty() }
        else awaitStateMatching { it.addText == text }
    }

    private suspend fun DetailTestContext.awaitAdded() =
        awaitStateMatching { it.addText.isEmpty() && !it.isAdding }

    @Test
    fun suggestionsShowMatchesAndHighlightTheExactOne() = runTest(testDispatcher) {
        repo.foodsResult = listOf(milk, aFood(id = "nut-milk", name = "nut milk")).right()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            type("milk", expectSuggestions = true)
            val state = viewModel.stateFlow.value
            assertThat(state.addSuggestions.map { it.id }).containsExactly("milk", "nut-milk")
            assertThat(state.addHighlightedId).isEqualTo("milk")
            assertThat(state.addSuggestionQuery).isEqualTo("milk")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun aSearchWithNoMatchesKeepsItsQueryAndClearingResetsIt() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            viewModel.onEvent(ShoppingListDetailIntent.OnAddTextChanged("za'atar"))
            val searched = awaitStateMatching { it.addSuggestionQuery.isNotEmpty() }
            assertThat(searched.addSuggestions).isEmpty()
            assertThat(searched.addSuggestionQuery).isEqualTo("za'atar")

            viewModel.onEvent(ShoppingListDetailIntent.OnAddTextChanged(""))
            awaitStateMatching { it.addText.isEmpty() && it.addSuggestionQuery.isEmpty() }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun exactMatchAddsTheFoodWithoutTheParser() = runTest(testDispatcher) {
        repo.foodsResult = listOf(milk).right()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            type("Milk", expectSuggestions = true)
            viewModel.onEvent(ShoppingListDetailIntent.OnAddItemSubmitted)
            val state = awaitAdded()

            assertThat(repo.addedFoodItems).containsExactly(AddedFoodItem(milk, 1.0, null, ""))
            assertThat(repo.parsedTexts).isEmpty()
            assertThat(state.addSuggestions).isEmpty()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun quantityAndUnitGoThroughTheParser() = runTest(testDispatcher) {
        repo.foodsResult = listOf(milk).right()
        repo.parseResult = parsed(quantity = 2.0, unit = true, unitName = "cup", food = true, foodName = "milk").right()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            type("2 cups milk", expectSuggestions = true)
            // The suggestions search just the food part.
            assertThat(repo.foodQueries).contains("milk")
            viewModel.onEvent(ShoppingListDetailIntent.OnAddItemSubmitted)
            awaitAdded()

            assertThat(repo.parsedTexts).containsExactly("2 cups milk")
            assertThat(repo.addedFoodItems).containsExactly(AddedFoodItem(milk, 2.0, cup, ""))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun parserFailureSavesAPlainNote() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            type("za'atar", expectSuggestions = false)
            viewModel.onEvent(ShoppingListDetailIntent.OnAddItemSubmitted)
            awaitAdded()

            assertThat(repo.addedItems).containsExactly("list-1" to "za'atar")
            assertThat(repo.addedFoodItems).isEmpty()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun parsedFoodThatMatchesNothingSavesAPlainNote() = runTest(testDispatcher) {
        repo.parseResult = parsed(quantity = 1.0, foodName = "zaatar").right()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            type("1 tbsp zaatar", expectSuggestions = false)
            viewModel.onEvent(ShoppingListDetailIntent.OnAddItemSubmitted)
            awaitAdded()

            assertThat(repo.addedItems).containsExactly("list-1" to "1 tbsp zaatar")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun parsedFoodNameIsLookedUpWhenTheParserDidNotMatchIt() = runTest(testDispatcher) {
        repo.foodsResult = listOf(milk).right()
        repo.parseResult = parsed(quantity = 3.0, foodName = "milk").right()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            type("3 milk", expectSuggestions = true)
            viewModel.onEvent(ShoppingListDetailIntent.OnAddItemSubmitted)
            awaitAdded()

            assertThat(repo.addedFoodItems).containsExactly(AddedFoodItem(milk, 3.0, null, ""))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun unknownUnitIsKeptInTheNote() = runTest(testDispatcher) {
        repo.foodsResult = listOf(milk).right()
        repo.parseResult = parsed(quantity = 1.0, unitName = "splash", food = true, note = "oat").right()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            type("1 splash milk, oat", expectSuggestions = false)
            viewModel.onEvent(ShoppingListDetailIntent.OnAddItemSubmitted)
            awaitAdded()

            assertThat(repo.addedFoodItems).containsExactly(AddedFoodItem(milk, 1.0, null, "splash oat"))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun pickingASuggestionKeepsTheTypedQuantity() = runTest(testDispatcher) {
        repo.foodsResult = listOf(milk).right()
        repo.parseResult = parsed(quantity = 2.0, unit = true, unitName = "cup", foodName = "mil").right()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            type("2 cups mil", expectSuggestions = true)
            viewModel.onEvent(ShoppingListDetailIntent.OnAddSuggestionPicked(FoodOptionUi("milk", UiText.Dynamic("milk"))))
            awaitAdded()

            assertThat(repo.addedFoodItems).containsExactly(AddedFoodItem(milk, 2.0, cup, ""))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun pickingASuggestionStillAddsItWhenTheParserFails() = runTest(testDispatcher) {
        repo.foodsResult = listOf(milk).right()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            type("2 cups mil", expectSuggestions = true)
            viewModel.onEvent(ShoppingListDetailIntent.OnAddSuggestionPicked(FoodOptionUi("milk", UiText.Dynamic("milk"))))
            awaitAdded()

            assertThat(repo.addedFoodItems).containsExactly(AddedFoodItem(milk, 1.0, null, ""))
            cancelAndIgnoreRemainingItems()
        }
    }
}
