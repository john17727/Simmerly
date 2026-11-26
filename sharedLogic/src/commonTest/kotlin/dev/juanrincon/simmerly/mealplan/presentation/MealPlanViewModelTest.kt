package dev.juanrincon.simmerly.mealplan.presentation

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.mealplan.FakeMealPlanRepository
import dev.juanrincon.simmerly.mealplan.aPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealPlanError
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanSideEffect
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.shoppinglists.awaitSideEffectSkippingStates
import dev.juanrincon.simmerly.shoppinglists.awaitStateMatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class MealPlanViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeMealPlanRepository
    private lateinit var viewModel: MealPlanViewModel

    /** Thursday 2026-11-26. */
    private val today = LocalDate(2026, 11, 26)
    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-11-26T12:00:00Z")
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeMealPlanRepository()
        viewModel = MealPlanViewModel(repo, clock, TimeZone.UTC)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val dinner = aPlanEntry(id = 1, date = today, type = MealType.Dinner)
    private val anyDay = aPlanEntry(
        id = 2, date = LocalDate(2026, 11, 23), endDate = LocalDate(2026, 11, 29),
        type = MealType.Lunch, recipeId = "r2", recipeName = "Soup",
    )
    private val note = aPlanEntry(id = 3, date = today, type = MealType.Snack, recipeId = null, title = "Fruit")

    private fun loaded(vararg e: dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry) =
        Either.Right(LoadingResult.Loaded(e.toList()))

    @Test
    fun startsOnTodayLoading() {
        val state = viewModel.container.stateFlow.value
        assertThat(state.selectedDate).isEqualTo(today)
        assertThat(state.isLoading).isTrue()
    }

    @Test
    fun loadedEntriesFillTheDaySectionsAnyDayStripAndDots() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.planFlow.emit(loaded(dinner, anyDay, note))

            val state = awaitStateMatching { !it.isLoading }
            assertThat(state.daySections.first { it.type == MealType.Dinner }.entries.map { it.id }).isEqualTo(listOf(1))
            assertThat(state.daySections.first { it.type == MealType.Snack }.entries.map { it.id }).isEqualTo(listOf(3))
            assertThat(state.anyDayEntries.map { it.id }).isEqualTo(listOf(2))
            assertThat(state.plannedDays).isEqualTo(setOf(today))
            assertThat(state.dayRecipeEntries.map { it.id }).isEqualTo(listOf(1))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun theWeekFollowsTheHouseholdFirstDay() = runTest(dispatcher) {
        repo.firstDay.value = DayOfWeek.SUNDAY
        viewModel.testWithInternalState(this) {
            runOnCreate()
            val state = awaitStateMatching { it.firstDay == DayOfWeek.SUNDAY }
            assertThat(state.weekDays.first()).isEqualTo(LocalDate(2026, 11, 22))
            assertThat(state.weekDays.last()).isEqualTo(LocalDate(2026, 11, 28))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun aFailedFirstLoadShowsTheErrorState() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.planFlow.emit(Either.Left(MealPlanError.FetchError))

            val state = awaitStateMatching { it.hasError }
            assertThat(state.isLoading).isFalse()
            assertThat(state.isOffline).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun aFailedRefreshWithCachedDataGoesOfflineAndPausesEdits() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.planFlow.emit(loaded(dinner))
            awaitStateMatching { !it.isLoading }
            repo.planFlow.emit(Either.Left(MealPlanError.FetchError))

            val state = awaitStateMatching { it.isOffline }
            assertThat(state.canEdit).isFalse()
            assertThat(state.hasError).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun selectingADayInAnotherMonthObservesThatMonth() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            val before = repo.observedRanges.size
            viewModel.onEvent(MealPlanIntent.SelectDate(LocalDate(2027, 1, 15)))
            awaitStateMatching { it.selectedDate == LocalDate(2027, 1, 15) }
            assertThat(repo.observedRanges.size > before).isTrue()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun stepWeeksAndGoToTodayMoveTheSelection() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            viewModel.onEvent(MealPlanIntent.StepWeeks(1))
            awaitStateMatching { it.selectedDate == LocalDate(2026, 12, 3) }
            viewModel.onEvent(MealPlanIntent.GoToToday)
            awaitStateMatching { it.selectedDate == today }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun theServingsStepperUpdatesTheScale() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.planFlow.emit(loaded(dinner))
            awaitStateMatching { !it.isLoading }
            viewModel.onEvent(MealPlanIntent.ChangeServings(1, +1))
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.updated.single().recipeScale).isEqualTo(1.25)
    }

    @Test
    fun servingsNeverDropBelowOne() = runTest(dispatcher) {
        val one = aPlanEntry(id = 1, date = today, recipeServings = 4.0, scale = 0.25)
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.planFlow.emit(loaded(one))
            awaitStateMatching { !it.isLoading }
            viewModel.onEvent(MealPlanIntent.ChangeServings(1, -1))
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.updated).hasSize(0)
    }

    @Test
    fun editsDoNothingWhileOffline() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.planFlow.emit(loaded(dinner))
            awaitStateMatching { !it.isLoading }
            repo.planFlow.emit(Either.Left(MealPlanError.FetchError))
            awaitStateMatching { it.isOffline }
            viewModel.onEvent(MealPlanIntent.ChangeServings(1, +1))
            viewModel.onEvent(MealPlanIntent.Remove(1))
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.updated).hasSize(0)
        assertThat(repo.removedIds).hasSize(0)
    }

    @Test
    fun cookMovesAnAnyDayEntryOntoTheSelectedDayWithUndo() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.planFlow.emit(loaded(anyDay))
            awaitStateMatching { !it.isLoading }
            viewModel.onEvent(MealPlanIntent.CookOnSelectedDay(2))

            val effect = awaitSideEffectSkippingStates()
            assertThat(effect).isInstanceOf(MealPlanSideEffect.ShowUndo::class)
            assertThat((effect as MealPlanSideEffect.ShowUndo).undo).isEqualTo(MealPlanIntent.UndoMove(anyDay))
            cancelAndIgnoreRemainingItems()
        }
        val moved = repo.updated.single()
        assertThat(moved.date).isEqualTo(today)
        assertThat(moved.endDate).isNull()
    }

    @Test
    fun undoMoveRestoresTheOriginalEntry() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            viewModel.onEvent(MealPlanIntent.UndoMove(anyDay))
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.updated.single()).isEqualTo(anyDay)
    }

    @Test
    fun removeDeletesAndOffersUndoWhichAddsItBack() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.planFlow.emit(loaded(dinner))
            awaitStateMatching { !it.isLoading }
            viewModel.onEvent(MealPlanIntent.Remove(1))
            val effect = awaitSideEffectSkippingStates() as MealPlanSideEffect.ShowUndo
            viewModel.onEvent(effect.undo)
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.removedIds).isEqualTo(listOf(1))
        val draft = repo.added.single()
        assertThat(draft.recipeId).isEqualTo("r1")
        assertThat(draft.date).isEqualTo(today)
        assertThat(draft.type).isEqualTo(MealType.Dinner)
    }

    @Test
    fun aFailedSaveShowsAMessage() = runTest(dispatcher) {
        repo.saveFails = true
        viewModel.testWithInternalState(this) {
            runOnCreate()
            repo.planFlow.emit(loaded(dinner))
            awaitStateMatching { !it.isLoading }
            viewModel.onEvent(MealPlanIntent.ChangeServings(1, +1))
            assertThat(awaitSideEffectSkippingStates()).isInstanceOf(MealPlanSideEffect.ShowMessage::class)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun pullToRefreshRefreshesTheMonthWindow() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            viewModel.onEvent(MealPlanIntent.Refresh)
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.refreshCount).isEqualTo(1)
        assertThat(viewModel.container.stateFlow.value.isPullRefreshing).isFalse()
    }
}
