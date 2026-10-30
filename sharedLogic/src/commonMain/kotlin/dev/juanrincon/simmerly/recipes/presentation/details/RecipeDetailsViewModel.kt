package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.initialload.domain.UserRepository
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.details.mappers.toRecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.withServings
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListRowUi
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListStep
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListTarget
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsSideEffect
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeTab
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

class RecipeDetailsViewModel(
    private val recipeId: String,
    private val repository: RecipeRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val userRepository: UserRepository,
) : OrbitContainerHost<RecipeDetailsState, RecipeDetailsState, RecipeDetailsSideEffect>,
    ViewModel() {

    override val container: OrbitContainer<RecipeDetailsState, RecipeDetailsState, RecipeDetailsSideEffect> =
        orbitContainer(initialState = RecipeDetailsState()) {
            observeRecipe()
        }

    /** The lists as last observed, before the sheet's Mine/Household filter. */
    private var rawLists: List<ShoppingList> = emptyList()
    private var currentUserId: String? = null
    private var observingLists = false

    /** The recipe's ingredients as Mealie has them, unscaled; the shopping list API takes these back. */
    private var sourceIngredients: List<Ingredient> = emptyList()

    // Concrete, non-generic accessors for Swift — see WelcomeViewModel for why these are needed.
    val stateFlow: StateFlow<RecipeDetailsState> get() = container.stateFlow
    val sideEffectFlow: Flow<RecipeDetailsSideEffect> get() = container.sideEffectFlow

    fun onEvent(event: RecipeDetailsIntent) {
        when (event) {
            RecipeDetailsIntent.AddServing -> updateServing(
                container.stateFlow.value.recipe.servings + 1
            )
            RecipeDetailsIntent.RemoveServing -> updateServing(
                container.stateFlow.value.recipe.servings - 1
            )
            RecipeDetailsIntent.ShowSettings -> intent {
                reduce { state.copy(showSettings = true) }
            }
            RecipeDetailsIntent.DismissSettings -> intent {
                reduce { state.copy(showSettings = false) }
            }
            is RecipeDetailsIntent.UpdateSettings -> updateSettings(event)
            else -> onAddToListEvent(event)
        }
    }

    private fun observeRecipe() = intent {
        repository.recipeDetails(recipeId)
            .distinctUntilChanged()
            .collect { response ->
                response.fold(
                    ifLeft = { error ->
                        if (state.recipe == RecipeDetailUi.emptyRecipe) {
                            reduce { state.copy(loading = false, isRefreshing = false, error = error) }
                        } else {
                            reduce { state.copy(isRefreshing = false) }
                        }
                    },
                    ifRight = { result ->
                        when (result) {
                            LoadingResult.Loading -> reduce { state.copy(loading = true) }
                            LoadingResult.Refreshing -> reduce { state.copy(isRefreshing = true) }
                            LoadingResult.RefreshComplete -> reduce { state.copy(isRefreshing = false) }
                            is LoadingResult.Loaded -> {
                                val recipe = result.data.toRecipeDetailUi()
                                sourceIngredients = result.data.ingredients
                                reduce {
                                    state.copy(
                                        loading = false,
                                        isRefreshing = false,
                                        error = null,
                                        recipe = recipe,
                                        baseServings = recipe.servings,
                                        mobileTabs = buildList {
                                            add(RecipeTab.Overview)
                                            add(RecipeTab.Ingredients)
                                            add(RecipeTab.Instructions)
                                            if (recipe.notes.isNotEmpty()) add(RecipeTab.Notes)
                                            if (recipe.settings.showNutrition) add(RecipeTab.Nutrition)
                                        },
                                        desktopTabs = buildList {
                                            add(RecipeTab.Recipe)
                                            if (recipe.notes.isNotEmpty()) add(RecipeTab.Notes)
                                            if (!recipe.settings.disableComments) add(RecipeTab.Comments)
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
    }

    private fun updateServing(newServings: Double) = intent {
        val current = state.recipe
        if (current == RecipeDetailUi.emptyRecipe) return@intent

        val updated = current.withServings(newServings)
        if (updated == current) return@intent

        reduce { state.copy(recipe = updated) }
    }

    private fun updateSettings(event: RecipeDetailsIntent.UpdateSettings) = intent {
        // Optimistic update — reflect the toggle immediately in the UI
        reduce { state.copy(recipe = state.recipe.copy(settings = event.settings)) }
        repository.updateSettings(state.recipe.id, event.settings)
        // On error the next recipeDetails emission will reconcile the state
    }
    // region Add to shopping list

    private fun onAddToListEvent(event: RecipeDetailsIntent) {
        when (event) {
            RecipeDetailsIntent.OpenAddToList -> openAddToList()
            RecipeDetailsIntent.DismissAddToList -> intent {
                if (state.addToList?.isSubmitting == true) return@intent
                reduce { state.copy(addToList = null) }
            }
            RecipeDetailsIntent.AddToListBack -> intent {
                val sheet = state.addToList ?: return@intent
                val previous = when (sheet.step) {
                    AddToListStep.ChooseList -> null
                    AddToListStep.NewListName -> AddToListStep.ChooseList
                    AddToListStep.PickIngredients ->
                        if (sheet.target is AddToListTarget.New) AddToListStep.NewListName
                        else AddToListStep.ChooseList
                }
                if (previous != null && !sheet.isSubmitting) {
                    reduce { state.updateSheet { copy(step = previous) } }
                }
            }
            is RecipeDetailsIntent.AddToListFilterChanged -> intent {
                reduce { state.updateSheet { copy(filter = event.filter) }.withVisibleRows() }
            }
            is RecipeDetailsIntent.AddToListListChosen -> intent {
                val list = rawLists.firstOrNull { it.id == event.listId } ?: return@intent
                reduce {
                    state.updateSheet {
                        copy(
                            step = AddToListStep.PickIngredients,
                            target = AddToListTarget.Existing(list.id, list.name),
                        )
                    }
                }
            }
            RecipeDetailsIntent.AddToListNewListClicked -> intent {
                reduce { state.updateSheet { copy(step = AddToListStep.NewListName) } }
            }
            is RecipeDetailsIntent.AddToListNameChanged -> intent {
                reduce { state.updateSheet { copy(newListName = event.name) } }
            }
            RecipeDetailsIntent.AddToListNameConfirmed -> intent {
                val name = state.addToList?.newListName?.trim().orEmpty()
                if (name.isEmpty()) return@intent
                reduce {
                    state.updateSheet {
                        copy(step = AddToListStep.PickIngredients, target = AddToListTarget.New(name))
                    }
                }
            }
            RecipeDetailsIntent.AddToListAddServing -> intent {
                reduce { state.updateSheet { copy(servings = servings + 1) } }
            }
            RecipeDetailsIntent.AddToListRemoveServing -> intent {
                reduce { state.updateSheet { copy(servings = (servings - 1).coerceAtLeast(1.0)) } }
            }
            is RecipeDetailsIntent.AddToListToggleIngredient -> intent {
                reduce {
                    state.updateSheet {
                        copy(selected = if (event.index in selected) selected - event.index else selected + event.index)
                    }
                }
            }
            RecipeDetailsIntent.AddToListToggleAll -> intent {
                val all = state.recipe.ingredients.indices.toSet()
                reduce {
                    state.updateSheet { copy(selected = if (selected == all) emptySet() else all) }
                }
            }
            RecipeDetailsIntent.AddToListConfirm -> confirmAddToList()
            else -> Unit
        }
    }

    private fun openAddToList() = intent {
        if (state.addToList != null || state.recipe == RecipeDetailUi.emptyRecipe) return@intent
        reduce {
            state.copy(
                addToList = AddToListState(
                    servings = state.recipe.servings,
                    selected = state.recipe.ingredients.indices.toSet(),
                )
            ).withVisibleRows()
        }
        if (!observingLists) {
            observingLists = true
            observeShoppingLists()
        }
        if (currentUserId == null) {
            userRepository.currentUserId().onRight { id ->
                currentUserId = id
                reduce { state.withVisibleRows() }
            }
        }
        shoppingListRepository.refreshLists().onLeft {
            postSideEffect(RecipeDetailsSideEffect.ShowMessage(UiText.Resource(StringKey.ShoppingListLoadFailed)))
        }
    }

    private fun observeShoppingLists() = intent {
        shoppingListRepository.observeLists().collect { lists ->
            rawLists = lists.orEmpty()
            reduce { state.withVisibleRows(loaded = lists != null) }
        }
    }

    /** "Mine" shows everything until the signed-in user is known, as the shopping lists overview does. */
    private fun RecipeDetailsState.withVisibleRows(loaded: Boolean = rawLists.isNotEmpty()): RecipeDetailsState {
        val sheet = addToList ?: return this
        val rows = rawLists
            .filter { sheet.filter == ListFilter.Household || currentUserId == null || it.userId == currentUserId }
            .map { list ->
                AddToListRowUi(
                    id = list.id,
                    name = list.name,
                    itemCount = list.items.size,
                    alreadyHasRecipe = list.recipes.any { it.recipeId == recipeId },
                )
            }
        return copy(addToList = sheet.copy(lists = if (loaded || rows.isNotEmpty()) rows else sheet.lists))
    }

    private fun RecipeDetailsState.updateSheet(transform: AddToListState.() -> AddToListState): RecipeDetailsState =
        copy(addToList = addToList?.transform())

    private fun confirmAddToList() = intent {
        val sheet = state.addToList ?: return@intent
        val target = sheet.target ?: return@intent
        if (sheet.isSubmitting || sheet.selected.isEmpty()) return@intent
        reduce { state.updateSheet { copy(isSubmitting = true) } }

        val recipe = state.recipe
        val baseServings = state.baseServings

        val listId = when (target) {
            is AddToListTarget.Existing -> target.id
            is AddToListTarget.New -> shoppingListRepository.createList(target.name).fold(
                ifLeft = {
                    reduce { state.updateSheet { copy(isSubmitting = false) } }
                    postSideEffect(updateFailed())
                    return@intent
                },
                ifRight = { it.id },
            )
        }

        // The recipe is linked either way, as in Mealie's web app. Mealie scales by a multiple of the
        // recipe's own servings and takes just the ingredients to add when not every one is ticked.
        val ticked = recipe.ingredients.filterIndexed { index, _ -> index in sheet.selected }
            .map { it.referenceId }.toSet()
        val chosen = sourceIngredients.filter { it.referenceId in ticked }
        val result = shoppingListRepository.addRecipe(
            listId = listId,
            recipeId = recipeId,
            scale = if (baseServings > 0) sheet.servings / baseServings else 1.0,
            ingredients = chosen.takeUnless { sheet.selected.size == recipe.ingredients.size },
        )

        result.fold(
            ifLeft = {
                // A list we just created stays; the next attempt adds to it rather than creating another.
                reduce {
                    state.updateSheet {
                        copy(
                            isSubmitting = false,
                            target = AddToListTarget.Existing(listId, target.name),
                            step = AddToListStep.PickIngredients,
                        )
                    }
                }
                postSideEffect(updateFailed())
            },
            ifRight = {
                reduce { state.copy(addToList = null) }
                postSideEffect(RecipeDetailsSideEffect.ItemsAdded(addedMessage(target, sheet.selected.size), listId))
            },
        )
    }

    private fun addedMessage(target: AddToListTarget, count: Int): UiText = when (target) {
        is AddToListTarget.Existing ->
            if (count == 1) UiText.Resource(StringKey.ItemAddedToList, listOf(target.name))
            else UiText.Resource(StringKey.ItemsAddedToList, listOf(count.toString(), target.name))
        is AddToListTarget.New ->
            if (count == 1) UiText.Resource(StringKey.ListCreatedWithItem, listOf(target.name))
            else UiText.Resource(StringKey.ListCreatedWithItems, listOf(target.name, count.toString()))
    }

    private fun updateFailed() =
        RecipeDetailsSideEffect.ShowMessage(UiText.Resource(StringKey.ShoppingListUpdateFailed))

    // endregion
}
