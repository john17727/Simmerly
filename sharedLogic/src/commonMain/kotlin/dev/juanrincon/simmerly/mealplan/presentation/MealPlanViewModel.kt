package dev.juanrincon.simmerly.mealplan.presentation

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.mealplan.domain.MealPlanDraft
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealPlanRepository
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanSideEffect
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanState
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Job
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class MealPlanViewModel(
    private val repository: MealPlanRepository,
    clock: Clock = Clock.System,
    zone: TimeZone = TimeZone.currentSystemDefault(),
) : OrbitContainerHost<MealPlanState, MealPlanState, MealPlanSideEffect>, ViewModel() {

    private val today = clock.todayIn(zone)

    /** What is being viewed: the selected date and the week start it implies. */
    private val view = MutableStateFlow(today to DayOfWeek.MONDAY)

    override val container: OrbitContainer<MealPlanState, MealPlanState, MealPlanSideEffect> =
        orbitContainer(initialState = MealPlanState(today = today, selectedDate = today)) {
            observeFirstDay()
            observePlan()
            refreshPreferences()
        }

    // Concrete, non-generic accessors for Swift.
    val stateFlow: StateFlow<MealPlanState> get() = container.stateFlow
    val sideEffectFlow: Flow<MealPlanSideEffect> get() = container.sideEffectFlow

    fun onEvent(event: MealPlanIntent) {
        when (event) {
            is MealPlanIntent.SelectDate -> select(event.date)
            MealPlanIntent.GoToToday -> select(today)
            is MealPlanIntent.StepWeeks -> select(container.stateFlow.value.selectedDate.plusDays(7 * event.weeks))
            MealPlanIntent.Refresh -> refresh()
            is MealPlanIntent.ChangeServings -> changeServings(event.entryId, event.delta)
            is MealPlanIntent.CookOnSelectedDay -> cook(event.entryId)
            is MealPlanIntent.Remove -> remove(event.entryId)
            is MealPlanIntent.UndoRemove -> undoRemove(event.entry)
            is MealPlanIntent.UndoMove -> undoMove(event.entry)
        }
    }

    /** For SwiftUI's `.refreshable`; Compose sends [MealPlanIntent.Refresh]. */
    suspend fun refreshAndAwait() {
        refresh().join()
    }

    private fun select(date: LocalDate) {
        view.value = date to view.value.second
        intent { reduce { state.copy(selectedDate = date) } }
    }

    private fun observeFirstDay() = intent {
        repository.observeFirstDayOfWeek().collect { first ->
            view.value = view.value.first to first
            reduce { state.copy(firstDay = first) }
        }
    }

    private fun refreshPreferences() = intent { repository.refreshFirstDayOfWeek() }

    /** The cached window is the month grid around the selected date, so the calendar dots work. */
    private fun observePlan() = intent {
        view.map { (date, first) -> date.monthGridStart(first) }
            .distinctUntilChanged()
            .flatMapLatest { start ->
                reduce { state.copy(isLoading = true, hasError = false, isOffline = false) }
                repository.observe(start, start.plusDays(MONTH_GRID_DAYS - 1))
            }
            .collect { result ->
                var failed = false
                result.fold(
                    ifLeft = {
                        failed = true
                        val hasData = state.entries.isNotEmpty()
                        reduce {
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                isOffline = hasData,
                                hasError = !hasData,
                            )
                        }
                    },
                    ifRight = { loading ->
                        when (loading) {
                            LoadingResult.Loading -> reduce { state.copy(isLoading = true) }
                            LoadingResult.Refreshing -> reduce { state.copy(isRefreshing = true) }
                            // A failure inside this refresh already set isOffline/hasError.
                            LoadingResult.RefreshComplete -> reduce { state.copy(isRefreshing = false) }
                            is LoadingResult.Loaded -> reduce {
                                state.copy(entries = loading.data, isLoading = false)
                            }
                        }
                    },
                )
            }
    }

    private fun refresh(): Job = intent {
        if (state.isPullRefreshing) return@intent
        reduce { state.copy(isPullRefreshing = true) }
        val start = state.selectedDate.monthGridStart(state.firstDay)
        repository.refresh(start, start.plusDays(MONTH_GRID_DAYS - 1)).fold(
            ifLeft = {
                val hasData = state.entries.isNotEmpty()
                reduce { state.copy(isOffline = hasData, hasError = !hasData) }
                if (hasData) postSideEffect(MealPlanSideEffect.ShowMessage(UiText.Resource(StringKey.MealPlanLoadFailed)))
            },
            ifRight = { reduce { state.copy(isOffline = false, hasError = false) } },
        )
        reduce { state.copy(isPullRefreshing = false) }
    }

    private fun changeServings(entryId: Int, delta: Int) = intent {
        if (!state.canEdit) return@intent
        val entry = state.entries.firstOrNull { it.id == entryId } ?: return@intent
        val recipe = entry.recipe ?: return@intent
        val current = recipe.servings * entry.recipeScale
        val next = (current + delta).coerceAtLeast(1.0)
        if (next == current) return@intent
        repository.update(entry.copy(recipeScale = next / recipe.servings)).onLeft {
            postSideEffect(MealPlanSideEffect.ShowMessage(UiText.Resource(StringKey.MealPlanSaveFailed)))
        }
    }

    private fun cook(entryId: Int) = intent {
        if (!state.canEdit) return@intent
        val entry = state.entries.firstOrNull { it.id == entryId && it.isWholeWeek } ?: return@intent
        val day = state.selectedDate
        repository.update(entry.copy(date = day, endDate = null)).fold(
            ifLeft = {
                postSideEffect(MealPlanSideEffect.ShowMessage(UiText.Resource(StringKey.MealPlanSaveFailed)))
            },
            ifRight = {
                val dayLabel = if (day == state.today) "today" else day.dayOfWeek.displayName()
                postSideEffect(
                    MealPlanSideEffect.ShowUndo(
                        UiText.Resource(StringKey.MealPlanMoved, listOf(entry.type.name.lowercase(), dayLabel)),
                        MealPlanIntent.UndoMove(entry),
                    )
                )
            },
        )
    }

    private fun remove(entryId: Int) = intent {
        if (!state.canEdit) return@intent
        val entry = state.entries.firstOrNull { it.id == entryId } ?: return@intent
        repository.remove(entryId).fold(
            ifLeft = {
                postSideEffect(MealPlanSideEffect.ShowMessage(UiText.Resource(StringKey.MealPlanSaveFailed)))
            },
            ifRight = {
                postSideEffect(
                    MealPlanSideEffect.ShowUndo(
                        UiText.Resource(StringKey.MealPlanRemoved, listOf(entry.recipe?.name ?: entry.title)),
                        MealPlanIntent.UndoRemove(entry),
                    )
                )
            },
        )
    }

    private fun undoRemove(entry: MealPlanEntry) = intent {
        repository.add(
            MealPlanDraft(
                date = entry.date,
                endDate = entry.endDate,
                type = entry.type,
                title = entry.title,
                text = entry.text,
                recipeId = entry.recipe?.id,
                recipeScale = entry.recipeScale,
            )
        ).onLeft {
            postSideEffect(MealPlanSideEffect.ShowMessage(UiText.Resource(StringKey.MealPlanSaveFailed)))
        }
    }

    private fun undoMove(entry: MealPlanEntry) = intent {
        repository.update(entry).onLeft {
            postSideEffect(MealPlanSideEffect.ShowMessage(UiText.Resource(StringKey.MealPlanSaveFailed)))
        }
    }
}
