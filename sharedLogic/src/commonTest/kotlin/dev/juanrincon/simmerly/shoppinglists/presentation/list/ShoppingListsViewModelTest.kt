package dev.juanrincon.simmerly.shoppinglists.presentation.list

import arrow.core.left
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.initialload.FakeUserRepository
import dev.juanrincon.simmerly.shoppinglists.FakeShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.aShoppingList
import dev.juanrincon.simmerly.shoppinglists.awaitSideEffectSkippingStates
import dev.juanrincon.simmerly.shoppinglists.awaitStateMatching
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListError
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListDialog
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ShoppingListsIntent
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ShoppingListsSideEffect
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
class ShoppingListsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeShoppingListRepository
    private lateinit var userRepo: FakeUserRepository
    private lateinit var viewModel: ShoppingListsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeShoppingListRepository()
        userRepo = FakeUserRepository()
        viewModel = ShoppingListsViewModel(repo, userRepo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun mineShowsOnlyTheSignedInUsersListsAndHouseholdShowsAll() = runTest(testDispatcher) {
        repo.lists.value = listOf(
            aShoppingList(id = "mine", userId = "user-1"),
            aShoppingList(id = "theirs", userId = "user-2"),
        )
        viewModel.testWithInternalState(this) {
            runOnCreate()
            val mine = awaitStateMatching { it.allLists != null && it.currentUserId != null }
            assertThat(mine.lists.map { it.id }).containsExactly("mine")

            viewModel.onEvent(ShoppingListsIntent.OnFilterSelected(ListFilter.Household))
            val household = awaitStateMatching { it.filter == ListFilter.Household }
            assertThat(household.lists.map { it.id }).containsExactly("mine", "theirs")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun searchFiltersListsByName() = runTest(testDispatcher) {
        repo.lists.value = listOf(aShoppingList(id = "a", name = "Costco run"), aShoppingList(id = "b", name = "Brunch"))
        viewModel.testWithInternalState(this) {
            runOnCreate()
            awaitStateMatching { it.allLists != null }
            viewModel.onEvent(ShoppingListsIntent.OnQueryChanged("cost"))
            val state = awaitStateMatching { it.query == "cost" }
            assertThat(state.lists.map { it.id }).containsExactly("a")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun failedLoadExposesAnError() = runTest(testDispatcher) {
        repo.refreshListsResult = ShoppingListError.FetchError.left()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            val state = awaitStateMatching { it.error != null }
            assertThat(state.isLoading).isEqualTo(false)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun creatingAListOpensIt() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(ShoppingListsIntent.OnNewListClicked)
            assertThat(awaitInternalState().dialog).isEqualTo(ListDialog.Create)

            viewModel.onEvent(ShoppingListsIntent.OnCreateConfirmed("  Costco run "))
            assertThat(awaitSideEffectSkippingStates()).isEqualTo(ShoppingListsSideEffect.OpenList("new-list"))
            assertThat(repo.createdNames).containsExactly("Costco run")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun renamingSendsTheNewNameForTheChosenList() = runTest(testDispatcher) {
        repo.lists.value = listOf(aShoppingList(id = "a", name = "Old"))
        viewModel.testWithInternalState(this) {
            runOnCreate()
            awaitStateMatching { it.allLists != null }
            viewModel.onEvent(ShoppingListsIntent.OnRenameClicked("a"))
            awaitStateMatching { it.dialog is ListDialog.Rename }
            viewModel.onEvent(ShoppingListsIntent.OnRenameConfirmed("New"))
            awaitStateMatching { it.dialog == ListDialog.None }
            assertThat(repo.renamed).containsExactly("a" to "New")
            cancelAndIgnoreRemainingItems()
        }
    }
}
