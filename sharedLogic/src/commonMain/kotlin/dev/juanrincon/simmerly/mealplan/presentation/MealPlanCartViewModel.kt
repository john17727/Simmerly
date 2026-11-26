package dev.juanrincon.simmerly.mealplan.presentation

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.initialload.domain.UserRepository
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealPlanRepository
import dev.juanrincon.simmerly.mealplan.presentation.orbit.CartEntry
import dev.juanrincon.simmerly.mealplan.presentation.orbit.CartScope
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartArgs
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartSideEffect
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartState
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListRowUi
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListTarget
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

/**
 * "Add meal plan to shopping list": the recipes of a day or the week, their ingredients (optionally
 * combined) and a list to add them to. Mealie merges matching items itself, one recipe at a time.
 */
class MealPlanCartViewModel(
    private val args: MealPlanCartArgs,
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val userRepository: UserRepository,
) : OrbitContainerHost<MealPlanCartState, MealPlanCartState, MealPlanCartSideEffect>, ViewModel() {

    private var plan: List<MealPlanEntry> = emptyList()
    private var firstDay: DayOfWeek = DayOfWeek.MONDAY
    private val inclusion = mutableMapOf<Int, Boolean>()
    private val requested = mutableSetOf<String>()
    private var rawLists: List<ShoppingList> = emptyList()
    private var currentUserId: String? = null

    override val container: OrbitContainer<MealPlanCartState, MealPlanCartState, MealPlanCartSideEffect> =
        orbitContainer(
            initialState = MealPlanCartState(scope = args.scope, date = args.date, weekStart = args.date, weekEnd = args.date)
        ) {
            observePlan()
            observeLists()
        }

    // Concrete, non-generic accessors for Swift.
    val stateFlow: StateFlow<MealPlanCartState> get() = container.stateFlow
    val sideEffectFlow: Flow<MealPlanCartSideEffect> get() = container.sideEffectFlow

    fun onEvent(event: MealPlanCartIntent) {
        when (event) {
            is MealPlanCartIntent.SetScope -> intent {
                reduce { state.copy(scope = event.scope, toggledKeys = emptySet()) }
                inclusion.clear()
                rebuild()
            }
            is MealPlanCartIntent.ToggleEntry -> intent {
                val current = state.candidates.firstOrNull { it.entry.id == event.entryId } ?: return@intent
                inclusion[event.entryId] = !current.included
                rebuild()
            }
            is MealPlanCartIntent.SetCombine -> intent { reduce { state.copy(combine = event.combine, toggledKeys = emptySet()) } }
            is MealPlanCartIntent.ToggleRow -> intent {
                reduce {
                    val keys = if (event.key in state.toggledKeys) state.toggledKeys - event.key
                    else state.toggledKeys + event.key
                    state.copy(toggledKeys = keys)
                }
            }
            MealPlanCartIntent.ToggleAll -> intent {
                reduce {
                    state.copy(toggledKeys = if (state.allSelected) state.rows.map { it.key }.toSet() else emptySet())
                }
            }
            is MealPlanCartIntent.SetFilter -> intent { reduce { withRows(state.copy(filter = event.filter)) } }
            is MealPlanCartIntent.SelectList -> intent {
                val list = rawLists.firstOrNull { it.id == event.listId } ?: return@intent
                reduce { state.copy(target = AddToListTarget.Existing(list.id, list.name), creatingNew = false) }
            }
            MealPlanCartIntent.SelectNewList -> intent { reduce { state.copy(target = null, creatingNew = true) } }
            is MealPlanCartIntent.SetNewListName -> intent { reduce { state.copy(newListName = event.name) } }
            MealPlanCartIntent.Confirm -> confirm()
        }
    }

    private fun observePlan() = intent {
        firstDay = mealPlanRepository.observeFirstDayOfWeek().first()
        val start = args.date.startOfWeek(firstDay)
        reduce { state.copy(weekStart = start, weekEnd = start.plusDays(6)) }
        mealPlanRepository.observe(start, start.plusDays(6)).collect { result ->
            result.onRight { loading ->
                if (loading is LoadingResult.Loaded) {
                    plan = loading.data
                    reduce { state.copy(isLoading = false) }
                    rebuild()
                }
            }
        }
    }

    private suspend fun org.orbitmvi.orbit.syntax.Syntax<MealPlanCartState, MealPlanCartSideEffect>.rebuild() {
        val s = state
        val week = s.weekStart..s.weekEnd
        val inScope: List<MealPlanEntry> = if (args.entryId != null) plan.filter { it.id == args.entryId } else when (s.scope) {
            CartScope.Day -> plan.filter { !it.isWholeWeek && it.date == s.date }
            CartScope.Week -> plan.filter { if (it.isWholeWeek) it.coversDay(s.weekStart) else it.date in week }
        }
        val candidates = inScope.filter { it.recipe != null }.map { e ->
            val unscheduled = e.isWholeWeek
            CartEntry(e, included = inclusion[e.id] ?: (args.entryId != null || !unscheduled), unscheduled = unscheduled)
        }
        reduce { state.copy(candidates = candidates, notes = inScope.filter { it.isNote }) }
        loadIngredients(candidates.mapNotNull { it.entry.recipe?.id }.distinct())
    }

    private fun loadIngredients(recipeIds: List<String>) {
        for (id in recipeIds) {
            if (!requested.add(id)) continue
            intent {
                val result = recipeRepository.recipeDetails(id).first { it.isLeft() || it.getOrNull() is LoadingResult.Loaded }
                val detail = (result.getOrNull() as? LoadingResult.Loaded)?.data
                if (detail != null) {
                    reduce { state.copy(ingredients = state.ingredients + (id to detail.ingredients)) }
                } else {
                    requested.remove(id)
                    postSideEffect(MealPlanCartSideEffect.ShowMessage(UiText.Resource(StringKey.MealPlanLoadFailed)))
                }
            }
        }
    }

    private fun observeLists() = intent {
        if (currentUserId == null) userRepository.currentUserId().onRight { currentUserId = it }
        intent { shoppingListRepository.refreshLists() }
        shoppingListRepository.observeLists().collect { lists ->
            rawLists = lists.orEmpty()
            reduce { withRows(state, loaded = lists != null) }
        }
    }

    private fun withRows(s: MealPlanCartState, loaded: Boolean = rawLists.isNotEmpty()): MealPlanCartState {
        val recipeIds = s.includedEntries.mapNotNull { it.entry.recipe?.id }.toSet()
        val rows = rawLists
            .filter { s.filter == ListFilter.Household || currentUserId == null || it.userId == currentUserId }
            .map { list ->
                AddToListRowUi(
                    id = list.id,
                    name = list.name,
                    itemCount = list.items.size,
                    alreadyHasRecipe = list.recipes.any { it.recipeId in recipeIds },
                )
            }
        return s.copy(lists = if (loaded || rows.isNotEmpty()) rows else s.lists)
    }

    private fun confirm() = intent {
        val s = state
        if (!s.canAdd) return@intent
        val target = if (s.creatingNew) AddToListTarget.New(s.newListName.trim()) else s.target ?: return@intent
        reduce { state.copy(isSubmitting = true) }

        val listId = when (target) {
            is AddToListTarget.Existing -> target.id
            is AddToListTarget.New -> shoppingListRepository.createList(target.name).fold(
                ifLeft = {
                    reduce { state.copy(isSubmitting = false) }
                    postSideEffect(MealPlanCartSideEffect.ShowMessage(UiText.Resource(StringKey.ShoppingListUpdateFailed)))
                    return@intent
                },
                ifRight = { it.id },
            )
        }

        // Per entry: the ticked ingredients (every row that includes one of its ingredients).
        val selectedRows = s.rows.filter { it.selected }
        for (c in s.includedEntries) {
            val all = s.ingredients[c.entry.recipe!!.id].orEmpty()
            val picked = selectedRows.flatMap { it.sources }.filter { it.entryId == c.entry.id }.map { it.ingredient }
            if (picked.isEmpty()) continue
            val result = shoppingListRepository.addRecipe(
                listId = listId,
                recipeId = c.entry.recipe.id,
                scale = c.entry.recipeScale,
                ingredients = picked.takeUnless { picked.size == all.size },
            )
            if (result.isLeft()) {
                // A list created just now stays; a retry adds to it instead of creating another.
                reduce {
                    state.copy(
                        isSubmitting = false,
                        target = AddToListTarget.Existing(listId, target.name),
                        creatingNew = false,
                    )
                }
                postSideEffect(MealPlanCartSideEffect.ShowMessage(UiText.Resource(StringKey.ShoppingListUpdateFailed)))
                return@intent
            }
        }
        reduce { state.copy(isSubmitting = false) }
        val count = selectedRows.size
        val message = when {
            target is AddToListTarget.New && count == 1 -> UiText.Resource(StringKey.ListCreatedWithItem, listOf(target.name))
            target is AddToListTarget.New -> UiText.Resource(StringKey.ListCreatedWithItems, listOf(target.name, count.toString()))
            count == 1 -> UiText.Resource(StringKey.ItemAddedToList, listOf(target.name))
            else -> UiText.Resource(StringKey.ItemsAddedToList, listOf(count.toString(), target.name))
        }
        postSideEffect(MealPlanCartSideEffect.Added(message, listId))
    }
}
