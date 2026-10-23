package dev.juanrincon.simmerly.shoppinglists.presentation.list

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.initialload.domain.UserRepository
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListDialog
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ShoppingListsIntent
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ShoppingListsSideEffect
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ShoppingListsState
import dev.juanrincon.simmerly.shoppinglists.presentation.mappers.toCardUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

class ShoppingListsViewModel(
    private val repository: ShoppingListRepository,
    private val userRepository: UserRepository,
) : OrbitContainerHost<ShoppingListsState, ShoppingListsState, ShoppingListsSideEffect>,
    ViewModel() {

    /** The lists as last observed; filtering and renaming need their raw names. */
    private var rawLists: List<ShoppingList> = emptyList()

    override val container: OrbitContainer<ShoppingListsState, ShoppingListsState, ShoppingListsSideEffect> =
        orbitContainer(initialState = ShoppingListsState()) {
            observeLists()
            loadCurrentUser()
            refresh()
        }

    // Concrete, non-generic accessors for Swift — see WelcomeViewModel for why these are needed.
    val stateFlow: StateFlow<ShoppingListsState> get() = container.stateFlow
    val sideEffectFlow: Flow<ShoppingListsSideEffect> get() = container.sideEffectFlow

    fun onEvent(event: ShoppingListsIntent) {
        when (event) {
            ShoppingListsIntent.OnRefresh -> refresh()

            is ShoppingListsIntent.OnFilterSelected -> intent {
                reduce { state.copy(filter = event.filter).withVisibleLists() }
            }

            ShoppingListsIntent.OnSearchToggled -> intent {
                reduce { state.copy(isSearching = !state.isSearching, query = "").withVisibleLists() }
            }

            is ShoppingListsIntent.OnQueryChanged -> intent {
                reduce { state.copy(query = event.query).withVisibleLists() }
            }

            is ShoppingListsIntent.OnListClicked -> intent {
                postSideEffect(ShoppingListsSideEffect.OpenList(event.listId))
            }

            ShoppingListsIntent.OnNewListClicked -> intent {
                reduce { state.copy(dialog = ListDialog.Create) }
            }

            is ShoppingListsIntent.OnRenameClicked -> intent {
                val list = rawLists.firstOrNull { it.id == event.listId } ?: return@intent
                reduce { state.copy(dialog = ListDialog.Rename(list.id, list.name)) }
            }

            is ShoppingListsIntent.OnDeleteClicked -> intent {
                val list = state.allLists?.firstOrNull { it.id == event.listId } ?: return@intent
                reduce { state.copy(dialog = ListDialog.ConfirmDelete(list.id, list.name)) }
            }

            ShoppingListsIntent.OnDialogDismissed -> intent {
                reduce { state.copy(dialog = ListDialog.None) }
            }

            is ShoppingListsIntent.OnCreateConfirmed -> createList(event.name)
            is ShoppingListsIntent.OnRenameConfirmed -> renameList(event.name)
            ShoppingListsIntent.OnDeleteConfirmed -> deleteList()
        }
    }

    private fun observeLists() = intent {
        repository.observeLists().collect { lists ->
            rawLists = lists.orEmpty()
            reduce { state.copy(allLists = lists?.map { it.toCardUi() }).withVisibleLists() }
        }
    }

    private fun loadCurrentUser() = intent {
        userRepository.currentUserId().onRight { id ->
            reduce { state.copy(currentUserId = id).withVisibleLists() }
        }
    }

    private fun refresh() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        repository.refreshLists().fold(
            ifLeft = {
                reduce {
                    state.copy(isLoading = false, error = UiText.Resource(StringKey.ShoppingListLoadFailed))
                }
            },
            ifRight = { reduce { state.copy(isLoading = false) } }
        )
    }

    private fun createList(name: String) = intent {
        if (name.isBlank()) return@intent
        reduce { state.copy(dialog = ListDialog.None) }
        repository.createList(name.trim()).fold(
            ifLeft = { postSideEffect(updateFailed()) },
            ifRight = { postSideEffect(ShoppingListsSideEffect.OpenList(it.id)) }
        )
    }

    private fun renameList(name: String) = intent {
        val dialog = state.dialog as? ListDialog.Rename ?: return@intent
        reduce { state.copy(dialog = ListDialog.None) }
        if (name.isBlank() || name.trim() == dialog.currentName) return@intent
        repository.renameList(dialog.listId, name.trim()).onLeft { postSideEffect(updateFailed()) }
    }

    private fun deleteList() = intent {
        val dialog = state.dialog as? ListDialog.ConfirmDelete ?: return@intent
        reduce { state.copy(dialog = ListDialog.None) }
        repository.deleteList(dialog.listId).onLeft { postSideEffect(updateFailed()) }
    }

    /** "Mine" shows everything until the signed-in user is known, rather than briefly nothing. */
    private fun ShoppingListsState.withVisibleLists(): ShoppingListsState {
        val visible = rawLists
            .filter { filter == ListFilter.Household || currentUserId == null || it.userId == currentUserId }
            .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
            .map { it.id }
            .toSet()
        return copy(lists = allLists.orEmpty().filter { it.id in visible })
    }

    private fun updateFailed() =
        ShoppingListsSideEffect.ShowMessage(UiText.Resource(StringKey.ShoppingListUpdateFailed))
}
