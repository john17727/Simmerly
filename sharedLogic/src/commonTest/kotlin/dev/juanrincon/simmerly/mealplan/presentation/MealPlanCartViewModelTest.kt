package dev.juanrincon.simmerly.mealplan.presentation

import arrow.core.Either
import arrow.core.left
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.initialload.FakeUserRepository
import dev.juanrincon.simmerly.mealplan.FakeMealPlanRepository
import dev.juanrincon.simmerly.mealplan.aPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.presentation.orbit.CartScope
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartArgs
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartSideEffect
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.recipes.aRecipeDetail
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListTarget
import dev.juanrincon.simmerly.shoppinglists.FakeShoppingListRepository
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
import kotlinx.datetime.LocalDate
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MealPlanCartViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var plan: FakeMealPlanRepository
    private lateinit var recipes: FakeRecipeRepository
    private lateinit var lists: FakeShoppingListRepository

    /** Wednesday; the week is Mon 23 .. Sun 29. */
    private val wed = LocalDate(2026, 11, 25)

    private fun ing(display: String, qty: Double?, id: String = display) = Ingredient(
        quantity = qty, unit = null, food = null, note = null, display = display,
        title = null, originalText = null, referenceId = id,
    )

    private val pizza = aPlanEntry(id = 1, date = wed, type = MealType.Lunch, recipeId = "pizza", recipeName = "Pizza", scale = 2.0)
    private val chicken = aPlanEntry(id = 2, date = wed, type = MealType.Dinner, recipeId = "chicken", recipeName = "Chicken")
    private val anyDay = aPlanEntry(id = 3, date = LocalDate(2026, 11, 23), endDate = LocalDate(2026, 11, 29), recipeId = "beef", recipeName = "Beef")
    private val tue = aPlanEntry(id = 4, date = LocalDate(2026, 11, 24), recipeId = "tacos", recipeName = "Tacos")
    private val salad = aPlanEntry(id = 5, date = wed, type = MealType.Side, recipeId = null, title = "Garden salad")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        plan = FakeMealPlanRepository()
        recipes = FakeRecipeRepository()
        lists = FakeShoppingListRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun vm(scope: CartScope = CartScope.Day) = MealPlanCartViewModel(
        MealPlanCartArgs(wed, scope), plan, recipes, lists, FakeUserRepository(),
    )

    private suspend fun emitPlan(vararg e: MealPlanEntry) =
        plan.planFlow.emit(Either.Right(LoadingResult.Loaded(e.toList())))

    private suspend fun emitIngredients(index: Int, vararg i: Ingredient) = recipes.recipeDetailsFlow(index)
        .emit(Either.Right(LoadingResult.Loaded(aRecipeDetail(ingredients = i.toList()))))

    @Test
    fun dayScopeListsTheDaysRecipesAndNotesAndSkipsOtherDays() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitPlan(pizza, chicken, salad, tue, anyDay)
            val state = awaitStateMatching { it.candidates.size == 2 }
            assertThat(state.candidates.map { it.entry.id }).isEqualTo(listOf(1, 2))
            assertThat(state.notes.map { it.id }).isEqualTo(listOf(5))
            assertThat(state.candidates.all { it.included }).isTrue()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun weekScopeIncludesTheWeeksRecipesAndShowsAnyDayOnesUnticked() = runTest(dispatcher) {
        val viewModel = vm(CartScope.Week)
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitPlan(pizza, tue, anyDay)
            val state = awaitStateMatching { it.candidates.size == 3 }
            assertThat(state.candidates.first { it.entry.id == 3 }.included).isFalse()
            assertThat(state.candidates.first { it.entry.id == 3 }.unscheduled).isTrue()
            assertThat(state.includedEntries.map { it.entry.id }).isEqualTo(listOf(1, 4))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun aSingleEntryCartHoldsJustThatEntryAndIncludesIt() = runTest(dispatcher) {
        val viewModel = MealPlanCartViewModel(
            MealPlanCartArgs(wed, CartScope.Day, entryId = 3), plan, recipes, lists, FakeUserRepository(),
        )
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitPlan(pizza, chicken, anyDay)
            val state = awaitStateMatching { it.candidates.isNotEmpty() }
            assertThat(state.candidates.map { it.entry.id }).isEqualTo(listOf(3))
            assertThat(state.candidates.single().included).isTrue()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun combiningSumsMatchingIngredientsScaledByEachEntry() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitPlan(pizza, chicken)
            awaitStateMatching { it.candidates.size == 2 }
            emitIngredients(0, ing("flour", 2.0), ing("yeast", 1.0))
            emitIngredients(1, ing("flour", 1.0), ing("lemon", 1.0))

            val state = awaitStateMatching { it.rows.size == 3 }
            val flour = state.rows.first { it.food == "flour" }
            assertThat(flour.quantity).isEqualTo(5.0) // 2 x2 (pizza scale 2) + 1
            assertThat(flour.note).isEqualTo("For Pizza, Chicken")
            assertThat(state.groups).hasSize(1)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun withoutCombiningEachRecipeKeepsItsOwnGroup() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitPlan(pizza, chicken)
            awaitStateMatching { it.candidates.size == 2 }
            emitIngredients(0, ing("flour", 2.0))
            emitIngredients(1, ing("flour", 1.0))
            awaitStateMatching { it.rows.size == 1 }

            viewModel.onEvent(MealPlanCartIntent.SetCombine(false))
            val state = awaitStateMatching { !it.combine }
            assertThat(state.groups.map { it.entry!!.id }).isEqualTo(listOf(1, 2))
            assertThat(state.rows).hasSize(2)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun unticking_a_recipe_removes_its_ingredients() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitPlan(pizza, chicken)
            awaitStateMatching { it.candidates.size == 2 }
            emitIngredients(0, ing("flour", 2.0))
            emitIngredients(1, ing("lemon", 1.0))
            awaitStateMatching { it.rows.size == 2 }

            viewModel.onEvent(MealPlanCartIntent.ToggleEntry(2))
            val state = awaitStateMatching { it.rows.size == 1 }
            assertThat(state.rows.single().food).isEqualTo("flour")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun rowsAndToggleAll() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitPlan(pizza)
            awaitStateMatching { it.candidates.size == 1 }
            emitIngredients(0, ing("flour", 2.0), ing("yeast", 1.0))
            awaitStateMatching { it.rows.size == 2 }

            viewModel.onEvent(MealPlanCartIntent.ToggleRow("c:flour|"))
            assertThat(awaitStateMatching { it.selectedCount == 1 }.allSelected).isFalse()
            viewModel.onEvent(MealPlanCartIntent.ToggleAll)
            assertThat(awaitStateMatching { it.allSelected }.selectedCount).isEqualTo(2)
            viewModel.onEvent(MealPlanCartIntent.ToggleAll)
            assertThat(awaitStateMatching { it.selectedCount == 0 }.canAdd).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun addingSendsEachRecipeWithItsScaleAndOnlyTheTickedIngredients() = runTest(dispatcher) {
        lists.lists.value = listOf(aShoppingList(id = "l1", name = "Weekly"))
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitPlan(pizza, chicken)
            awaitStateMatching { it.candidates.size == 2 }
            emitIngredients(0, ing("flour", 2.0), ing("yeast", 1.0))
            emitIngredients(1, ing("lemon", 1.0))
            awaitStateMatching { it.rows.size == 3 }
            viewModel.onEvent(MealPlanCartIntent.ToggleRow("c:yeast|"))
            viewModel.onEvent(MealPlanCartIntent.SelectList("l1"))
            awaitStateMatching { it.canAdd }

            viewModel.onEvent(MealPlanCartIntent.Confirm)

            val effect = awaitSideEffectSkippingStates() as MealPlanCartSideEffect.Added
            assertThat(effect.listId).isEqualTo("l1")
            cancelAndIgnoreRemainingItems()
        }
        val calls = lists.addedRecipes
        assertThat(calls.map { it.recipeId }).isEqualTo(listOf("pizza", "chicken"))
        assertThat(calls[0].scale).isEqualTo(2.0)
        assertThat(calls[0].ingredients!!.map { it.display }).isEqualTo(listOf("flour"))
        assertThat(calls[1].ingredients).isNull() // everything of that recipe ticked
    }

    @Test
    fun aNewListIsCreatedFirst() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitPlan(pizza)
            awaitStateMatching { it.candidates.size == 1 }
            emitIngredients(0, ing("flour", 2.0))
            awaitStateMatching { it.rows.size == 1 }
            viewModel.onEvent(MealPlanCartIntent.SelectNewList)
            viewModel.onEvent(MealPlanCartIntent.SetNewListName("Week 48"))
            awaitStateMatching { it.canAdd }
            viewModel.onEvent(MealPlanCartIntent.Confirm)
            assertThat(awaitSideEffectSkippingStates()).isInstanceOf(MealPlanCartSideEffect.Added::class)
            cancelAndIgnoreRemainingItems()
        }
        assertThat(lists.createdNames).isEqualTo(listOf("Week 48"))
        assertThat(lists.addedRecipes.single().listId).isEqualTo("new-list")
    }

    @Test
    fun aFailedAddKeepsTheCreatedListAsTheTargetAndShowsAMessage() = runTest(dispatcher) {
        lists.writeResult = ShoppingListError.UpdateError.left()
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitPlan(pizza)
            awaitStateMatching { it.candidates.size == 1 }
            emitIngredients(0, ing("flour", 2.0))
            awaitStateMatching { it.rows.size == 1 }
            viewModel.onEvent(MealPlanCartIntent.SelectNewList)
            viewModel.onEvent(MealPlanCartIntent.SetNewListName("Week 48"))
            awaitStateMatching { it.canAdd }
            viewModel.onEvent(MealPlanCartIntent.Confirm)

            assertThat(awaitSideEffectSkippingStates()).isInstanceOf(MealPlanCartSideEffect.ShowMessage::class)
            val state = viewModel.container.stateFlow.value
            assertThat(state.target).isEqualTo(AddToListTarget.Existing("new-list", "Week 48"))
            assertThat(state.creatingNew).isFalse()
            assertThat(state.isSubmitting).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }
}
