package dev.juanrincon.simmerly.shoppinglists.presentation.detail

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import dev.juanrincon.simmerly.shoppinglists.FakeShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.aFood
import dev.juanrincon.simmerly.shoppinglists.aLabel
import dev.juanrincon.simmerly.shoppinglists.aShoppingList
import dev.juanrincon.simmerly.shoppinglists.anItem
import dev.juanrincon.simmerly.shoppinglists.awaitSideEffectSkippingStates
import dev.juanrincon.simmerly.shoppinglists.awaitStateMatching
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListError
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.FoodOptionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemGrouping
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.SectionKind
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.DetailDialog
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailIntent
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailSideEffect
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

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeShoppingListRepository
    private lateinit var viewModel: ShoppingListDetailViewModel

    private val produce = aLabel("produce", "Produce")
    private val list = aShoppingList(
        items = listOf(
            anItem(id = "onion", food = aFood(name = "onion"), label = produce, position = 0),
            anItem(id = "napkins", note = "Napkins", position = 1),
            anItem(id = "cumin", note = "Cumin", checked = true, position = 2),
        ),
        labelOrder = listOf("produce"),
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeShoppingListRepository()
        repo.lists.value = listOf(list)
        viewModel = ShoppingListDetailViewModel("list-1", repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stateSummarisesTheObservedList() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            val state = awaitStateMatching { it.name.isNotEmpty() && !it.isLoading }
            assertThat(state.remainingCount).isEqualTo(2)
            assertThat(state.checkedCount).isEqualTo(1)
            assertThat(state.sections.map { it.kind }).containsExactly(SectionKind.Aisle, SectionKind.NoAisle)
            assertThat(state.checkedItems.map { it.id }).containsExactly("cumin")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun switchingToByRecipeRegroupsItems() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            awaitStateMatching { it.name.isNotEmpty() }
            viewModel.onEvent(ShoppingListDetailIntent.OnGroupingSelected(ItemGrouping.ByRecipe))
            val state = awaitStateMatching { it.grouping == ItemGrouping.ByRecipe }
            assertThat(state.sections.map { it.kind }).containsExactly(SectionKind.AddedByHand)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun togglingASectionCollapsesAndExpandsIt() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            val loaded = awaitStateMatching { it.sections.isNotEmpty() }
            val key = loaded.sections.first().key

            viewModel.onEvent(ShoppingListDetailIntent.OnSectionToggled(key))
            assertThat(awaitStateMatching { key in it.collapsedSections }.collapsedSections).isEqualTo(setOf(key))

            viewModel.onEvent(ShoppingListDetailIntent.OnSectionToggled(key))
            awaitStateMatching { it.collapsedSections.isEmpty() }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun toggleAllCollapsesEverySectionThenExpandsThemAgain() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            val loaded = awaitStateMatching { it.sections.isNotEmpty() }
            val keys = loaded.sections.map { it.key }.toSet()

            // With one section already collapsed, "Collapse all" still applies to every section.
            viewModel.onEvent(ShoppingListDetailIntent.OnSectionToggled(keys.first()))
            awaitStateMatching { it.collapsedSections.isNotEmpty() }
            viewModel.onEvent(ShoppingListDetailIntent.OnToggleAllSectionsClicked)
            val collapsed = awaitStateMatching { it.collapsedSections == keys }
            assertThat(collapsed.anySectionExpanded).isEqualTo(false)

            viewModel.onEvent(ShoppingListDetailIntent.OnToggleAllSectionsClicked)
            val expanded = awaitStateMatching { it.collapsedSections.isEmpty() }
            assertThat(expanded.anySectionExpanded).isEqualTo(true)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun checkAllChecksTheRemainingItemsAndUndoUnchecksOnlyThose() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            awaitStateMatching { it.name.isNotEmpty() }

            viewModel.onEvent(ShoppingListDetailIntent.OnCheckAllClicked)
            assertThat(awaitSideEffectSkippingStates()).isEqualTo(ShoppingListDetailSideEffect.ShowCheckedAll(2))
            viewModel.onEvent(ShoppingListDetailIntent.OnUndoCheckAllClicked)

            // "cumin" was already checked, so undo leaves it alone.
            assertThat(repo.checkedCalls).containsExactly(
                Triple("list-1", setOf("onion", "napkins"), true),
                Triple("list-1", setOf("onion", "napkins"), false),
            )
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun uncheckAllUnchecksEveryCheckedItem() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            awaitStateMatching { it.name.isNotEmpty() }
            viewModel.onEvent(ShoppingListDetailIntent.OnUncheckAllClicked)
            assertThat(repo.checkedCalls).containsExactly(Triple("list-1", setOf("cumin"), false))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun failedCheckShowsAMessage() = runTest(testDispatcher) {
        repo.writeResult = ShoppingListError.UpdateError.left()
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(ShoppingListDetailIntent.OnItemCheckedChanged("onion", true))
            assertThat(awaitSideEffectSkippingStates())
                .isInstanceOf(ShoppingListDetailSideEffect.ShowMessage::class)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun addingAnItemSendsTrimmedTextAndClearsTheField() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(ShoppingListDetailIntent.OnAddTextChanged("  Paper towels "))
            awaitStateMatching { it.addText.isNotEmpty() }
            viewModel.onEvent(ShoppingListDetailIntent.OnAddItemSubmitted)
            awaitStateMatching { it.addText.isEmpty() && !it.isAdding }
            assertThat(repo.addedItems).containsExactly("list-1" to "Paper towels")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun savingTheEditorUpdatesFoodAndTakesTheFoodsAisle() = runTest(testDispatcher) {
        repo.foodsResult = listOf(aFood(id = "food-milk", name = "milk", labelId = "dairy")).right()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            awaitStateMatching { it.name.isNotEmpty() }
            viewModel.onEvent(ShoppingListDetailIntent.OnEditItemClicked("napkins"))
            awaitStateMatching { it.editor != null }
            viewModel.onEvent(ShoppingListDetailIntent.OnEditorFoodQueryChanged("mil"))
            val searched = awaitStateMatching { it.foodSuggestions.isNotEmpty() }
            assertThat(searched.foodSuggestions).containsExactly(FoodOptionUi("food-milk", "milk"))

            viewModel.onEvent(ShoppingListDetailIntent.OnEditorFoodSelected(FoodOptionUi("food-milk", "milk")))
            viewModel.onEvent(ShoppingListDetailIntent.OnEditorNoteChanged("oat"))
            viewModel.onEvent(ShoppingListDetailIntent.OnEditorSaveClicked)
            awaitStateMatching { it.editor == null }

            val saved = repo.updatedItems.single()
            assertThat(saved.food?.id).isEqualTo("food-milk")
            assertThat(saved.labelId).isEqualTo("dairy")
            assertThat(saved.note).isEqualTo("oat")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun typingOverAChosenFoodUnchoosesIt() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            awaitStateMatching { it.name.isNotEmpty() }
            viewModel.onEvent(ShoppingListDetailIntent.OnEditItemClicked("onion"))
            awaitStateMatching { it.editor?.food != null }
            viewModel.onEvent(ShoppingListDetailIntent.OnEditorFoodQueryChanged("oni"))
            val state = awaitStateMatching { it.editor?.foodQuery == "oni" }
            assertThat(state.editor?.food).isEqualTo(null)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun deletingTheListNavigatesBack() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(ShoppingListDetailIntent.OnDeleteListClicked)
            awaitStateMatching { it.dialog == DetailDialog.ConfirmDelete }
            viewModel.onEvent(ShoppingListDetailIntent.OnDeleteListConfirmed)
            assertThat(awaitSideEffectSkippingStates()).isEqualTo(ShoppingListDetailSideEffect.NavigateBack)
            assertThat(repo.deletedLists).containsExactly("list-1")
            cancelAndIgnoreRemainingItems()
        }
    }
}
