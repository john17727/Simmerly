package dev.juanrincon.simmerly.shoppinglists.presentation.detail

import androidx.lifecycle.ViewModel
import arrow.core.Either
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListError
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingFood
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingUnit
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemEditorUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemGrouping
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.DetailDialog
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailIntent
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailSideEffect
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailState
import dev.juanrincon.simmerly.shoppinglists.presentation.mappers.addItemQuery
import dev.juanrincon.simmerly.shoppinglists.presentation.mappers.checkedItemsUi
import dev.juanrincon.simmerly.shoppinglists.presentation.mappers.formatEditableQuantity
import dev.juanrincon.simmerly.shoppinglists.presentation.mappers.isNamed
import dev.juanrincon.simmerly.shoppinglists.presentation.mappers.toLinkedRecipesUi
import dev.juanrincon.simmerly.shoppinglists.presentation.mappers.toOptionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.mappers.toSections
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

/** How long the food search waits after the last keystroke before querying the server. */
private const val FOOD_SEARCH_DEBOUNCE_MS = 300L

/** How long the add-item suggestions wait after the last keystroke. */
private const val ADD_SEARCH_DEBOUNCE_MS = 250L

class ShoppingListDetailViewModel(
    private val listId: String,
    private val repository: ShoppingListRepository,
) : OrbitContainerHost<ShoppingListDetailState, ShoppingListDetailState, ShoppingListDetailSideEffect>,
    ViewModel() {

    /** The list as last observed — the state only holds its display form, and item updates
     * need the full items to build their request bodies. */
    private var list: ShoppingList? = null
    private var units: List<ShoppingUnit> = emptyList()
    private var foodResults: List<ShoppingFood> = emptyList()
    private var foodSearch: Job? = null
    private var addResults: List<ShoppingFood> = emptyList()
    private var addSearch: Job? = null

    override val container: OrbitContainer<ShoppingListDetailState, ShoppingListDetailState, ShoppingListDetailSideEffect> =
        orbitContainer(initialState = ShoppingListDetailState()) {
            observeList()
            refresh()
        }

    // Concrete, non-generic accessors for Swift — see WelcomeViewModel for why these are needed.
    val stateFlow: StateFlow<ShoppingListDetailState> get() = container.stateFlow
    val sideEffectFlow: Flow<ShoppingListDetailSideEffect> get() = container.sideEffectFlow

    fun onEvent(event: ShoppingListDetailIntent) {
        when (event) {
            ShoppingListDetailIntent.OnRefresh -> refresh()

            is ShoppingListDetailIntent.OnGroupingSelected -> intent {
                reduce { state.copy(grouping = event.grouping).withList(list) }
            }

            is ShoppingListDetailIntent.OnItemCheckedChanged -> intent {
                repository.setChecked(listId, setOf(event.itemId), event.checked)
                    .onLeft { postSideEffect(updateFailed()) }
            }

            ShoppingListDetailIntent.OnUncheckAllClicked -> intent {
                val checked = list?.items.orEmpty().filter { it.checked }.map { it.id }.toSet()
                repository.setChecked(listId, checked, false).onLeft { postSideEffect(updateFailed()) }
            }

            ShoppingListDetailIntent.OnDeleteCheckedClicked -> intent {
                val checked = list?.items.orEmpty().filter { it.checked }.map { it.id }.toSet()
                repository.deleteItems(listId, checked).onLeft { postSideEffect(updateFailed()) }
            }

            ShoppingListDetailIntent.OnCheckedExpandedToggled -> intent {
                reduce { state.copy(checkedExpanded = !state.checkedExpanded) }
            }

            ShoppingListDetailIntent.OnToggleAllSectionsClicked -> intent {
                // Only the current grouping's sections change, so switching grouping keeps the
                // other grouping's sections as the user left them.
                val keys = state.sections.map { it.key }.toSet()
                reduce {
                    state.copy(
                        collapsedSections = if (state.anySectionExpanded) state.collapsedSections + keys
                        else state.collapsedSections - keys
                    )
                }
            }

            is ShoppingListDetailIntent.OnSectionToggled -> intent {
                val collapsed = state.collapsedSections
                reduce {
                    state.copy(
                        collapsedSections = if (event.sectionKey in collapsed) collapsed - event.sectionKey
                        else collapsed + event.sectionKey
                    )
                }
            }

            is ShoppingListDetailIntent.OnAddTextChanged -> {
                intent { reduce { state.copy(addText = event.text) } }
                searchAddSuggestions(event.text)
            }

            ShoppingListDetailIntent.OnAddItemSubmitted -> addItem(picked = null)

            is ShoppingListDetailIntent.OnAddSuggestionPicked -> {
                addResults.firstOrNull { it.id == event.food.id }?.let { addItem(picked = it) }
            }

            is ShoppingListDetailIntent.OnRecipeQuantityChanged -> intent {
                repository.changeRecipeQuantity(listId, event.recipeId, event.delta)
                    .onLeft { postSideEffect(updateFailed()) }
            }

            is ShoppingListDetailIntent.OnEditItemClicked -> openEditor(event.itemId)

            is ShoppingListDetailIntent.OnEditorQuantityChanged -> intent {
                reduce { state.copy(editor = state.editor?.copy(quantity = event.quantity)) }
            }

            is ShoppingListDetailIntent.OnEditorUnitSelected -> intent {
                reduce { state.copy(editor = state.editor?.copy(unitId = event.unitId)) }
            }

            is ShoppingListDetailIntent.OnEditorFoodQueryChanged -> searchFoods(event.query)

            is ShoppingListDetailIntent.OnEditorFoodSelected -> intent {
                foodSearch?.cancel()
                reduce {
                    state.copy(
                        editor = state.editor?.copy(food = event.food, foodQuery = event.food?.name.orEmpty()),
                        foodSuggestions = emptyList(),
                    )
                }
            }

            is ShoppingListDetailIntent.OnEditorNoteChanged -> intent {
                reduce { state.copy(editor = state.editor?.copy(note = event.note)) }
            }

            ShoppingListDetailIntent.OnEditorSaveClicked -> saveEditor()

            ShoppingListDetailIntent.OnEditorDeleteClicked -> intent {
                val editor = state.editor ?: return@intent
                reduce { state.copy(editor = null, foodSuggestions = emptyList()) }
                repository.deleteItems(listId, setOf(editor.itemId)).onLeft { postSideEffect(updateFailed()) }
            }

            ShoppingListDetailIntent.OnEditorDismissed -> intent {
                foodSearch?.cancel()
                reduce { state.copy(editor = null, foodSuggestions = emptyList()) }
            }

            ShoppingListDetailIntent.OnRenameClicked -> intent {
                reduce { state.copy(dialog = DetailDialog.Rename(state.name)) }
            }

            ShoppingListDetailIntent.OnDeleteListClicked -> intent {
                reduce { state.copy(dialog = DetailDialog.ConfirmDelete) }
            }

            ShoppingListDetailIntent.OnDialogDismissed -> intent {
                reduce { state.copy(dialog = DetailDialog.None) }
            }

            is ShoppingListDetailIntent.OnRenameConfirmed -> intent {
                reduce { state.copy(dialog = DetailDialog.None) }
                val name = event.name.trim()
                if (name.isEmpty() || name == state.name) return@intent
                repository.renameList(listId, name).onLeft { postSideEffect(updateFailed()) }
            }

            ShoppingListDetailIntent.OnDeleteListConfirmed -> intent {
                reduce { state.copy(dialog = DetailDialog.None) }
                repository.deleteList(listId).fold(
                    ifLeft = { postSideEffect(updateFailed()) },
                    ifRight = { postSideEffect(ShoppingListDetailSideEffect.NavigateBack) }
                )
            }
        }
    }

    private fun observeList() = intent {
        repository.observeList(listId).collect { observed ->
            list = observed
            reduce { state.withList(observed) }
        }
    }

    private fun refresh() = intent {
        reduce { state.copy(isLoading = list == null, error = null) }
        repository.refreshList(listId).fold(
            ifLeft = {
                reduce {
                    state.copy(isLoading = false, error = UiText.Resource(StringKey.ShoppingListLoadFailed))
                }
            },
            ifRight = { reduce { state.copy(isLoading = false) } }
        )
    }

    private fun searchAddSuggestions(text: String) {
        addSearch?.cancel()
        addSearch = intent {
            delay(ADD_SEARCH_DEBOUNCE_MS)
            val query = addItemQuery(text, loadUnits())
            if (query.term.isBlank()) {
                addResults = emptyList()
                reduce { state.copy(addSuggestions = emptyList(), addHighlightedId = null) }
                return@intent
            }
            repository.searchFoods(query.term).onRight { foods ->
                addResults = foods
                reduce {
                    state.copy(
                        addSuggestions = foods.map { it.toOptionUi() },
                        addHighlightedId = foods.firstOrNull { it.isNamed(query.term) }?.id,
                        addSuggestionQuery = query.term,
                    )
                }
            }
        }
    }

    /** Adds what's typed, as [picked] when the user chose a suggestion. */
    private fun addItem(picked: ShoppingFood?) = intent {
        val text = state.addText.trim()
        if (text.isEmpty() || state.isAdding) return@intent
        addSearch?.cancel()
        reduce { state.copy(isAdding = true) }
        resolveAndAdd(text, picked).fold(
            ifLeft = {
                reduce { state.copy(isAdding = false) }
                postSideEffect(updateFailed())
            },
            ifRight = {
                addResults = emptyList()
                reduce {
                    state.copy(isAdding = false, addText = "", addSuggestions = emptyList(), addHighlightedId = null)
                }
            }
        )
    }

    /**
     * Text that just names a food (picked, or an exact suggestion) adds that food. Anything more,
     * like "2 cups milk", goes through Mealie's ingredient parser for its quantity, unit and food;
     * if the parser is unavailable or finds no known food, the text is kept as a plain note.
     */
    private suspend fun resolveAndAdd(
        text: String,
        picked: ShoppingFood?
    ): Either<ShoppingListError, Unit> {
        val query = addItemQuery(text, loadUnits())
        if (!query.hasExtras) {
            val food = picked ?: addResults.firstOrNull { it.isNamed(query.term) }
            if (food != null) return repository.addFoodItem(listId, food, 1.0, null, "")
        }
        val parsed = repository.parseIngredient(text).getOrNull()
            ?: return if (picked != null) {
                repository.addFoodItem(listId, picked, 1.0, null, "")
            } else {
                repository.addItem(listId, text)
            }
        val food = picked ?: parsed.food ?: parsed.foodName?.let { findFoodNamed(it) }
            ?: return repository.addItem(listId, text)
        // A unit the server doesn't know can't be linked, so it stays readable in the note.
        val note = listOfNotNull(
            parsed.unitName?.takeIf { parsed.unit == null && it.isNotBlank() },
            parsed.note.takeIf { it.isNotBlank() },
        ).joinToString(" ")
        return repository.addFoodItem(
            listId = listId,
            food = food,
            quantity = parsed.quantity?.takeIf { it > 0 } ?: 1.0,
            unit = parsed.unit,
            note = note,
        )
    }

    private suspend fun findFoodNamed(name: String): ShoppingFood? =
        addResults.firstOrNull { it.isNamed(name) }
            ?: repository.searchFoods(name).getOrNull()?.firstOrNull { it.isNamed(name) }

    /** Units are only needed to read a typed unit, so they're fetched on first use. */
    private suspend fun loadUnits(): List<ShoppingUnit> {
        if (units.isEmpty()) repository.units().onRight { units = it }
        return units
    }

    private fun openEditor(itemId: String) = intent {
        val item = list?.items?.firstOrNull { it.id == itemId } ?: return@intent
        reduce {
            state.copy(
                editor = ItemEditorUi(
                    itemId = item.id,
                    quantity = formatEditableQuantity(item.quantity),
                    unitId = item.unit?.id,
                    foodQuery = item.food?.name.orEmpty(),
                    food = item.food?.toOptionUi(),
                    note = item.note,
                ),
                foodSuggestions = emptyList(),
            )
        }
        val loaded = loadUnits()
        reduce { state.copy(units = loaded.map { it.toOptionUi() }) }
    }

    private fun searchFoods(query: String) {
        foodSearch?.cancel()
        foodSearch = intent {
            reduce {
                val editor = state.editor
                // Typing over a chosen food un-chooses it, so what's saved matches what's shown.
                val food = editor?.food?.takeIf { it.name == query }
                state.copy(editor = editor?.copy(foodQuery = query, food = food))
            }
            if (query.isBlank()) {
                reduce { state.copy(foodSuggestions = emptyList()) }
                return@intent
            }
            delay(FOOD_SEARCH_DEBOUNCE_MS)
            repository.searchFoods(query.trim()).onRight { foods ->
                foodResults = foods
                reduce { state.copy(foodSuggestions = foods.map { it.toOptionUi() }) }
            }
        }
    }

    private fun saveEditor() = intent {
        val editor = state.editor ?: return@intent
        val item = list?.items?.firstOrNull { it.id == editor.itemId } ?: return@intent
        val food = when (val chosen = editor.food) {
            null -> null
            else -> foodResults.firstOrNull { it.id == chosen.id }
                ?: item.food?.takeIf { it.id == chosen.id }
        }
        val foodChanged = food?.id != item.food?.id
        val updated = item.copy(
            quantity = editor.quantity.replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0)
                ?: item.quantity,
            unit = units.firstOrNull { it.id == editor.unitId }
                ?: item.unit?.takeIf { it.id == editor.unitId },
            food = food,
            note = editor.note.trim(),
            // A new food brings its own aisle, as it does in Mealie's own editor.
            labelId = if (foodChanged && food?.labelId != null) food.labelId else item.labelId,
            label = if (foodChanged && food?.labelId != null) null else item.label,
        )
        reduce { state.copy(editor = editor.copy(isSaving = true)) }
        repository.updateItem(updated).fold(
            ifLeft = {
                reduce { state.copy(editor = state.editor?.copy(isSaving = false)) }
                postSideEffect(updateFailed())
            },
            ifRight = { reduce { state.copy(editor = null, foodSuggestions = emptyList()) } }
        )
    }

    private fun ShoppingListDetailState.withList(list: ShoppingList?): ShoppingListDetailState {
        if (list == null) return this
        val checked = list.items.count { it.checked }
        return copy(
            name = list.name,
            remainingCount = list.items.size - checked,
            checkedCount = checked,
            progress = if (list.items.isEmpty()) 0f else checked.toFloat() / list.items.size,
            linkedRecipes = list.toLinkedRecipesUi(),
            sections = list.toSections(grouping),
            checkedItems = list.checkedItemsUi(),
        )
    }

    private fun updateFailed() =
        ShoppingListDetailSideEffect.ShowMessage(UiText.Resource(StringKey.ShoppingListUpdateFailed))
}
