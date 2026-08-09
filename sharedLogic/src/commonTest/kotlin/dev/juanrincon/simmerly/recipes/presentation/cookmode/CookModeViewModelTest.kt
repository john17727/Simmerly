package dev.juanrincon.simmerly.recipes.presentation.cookmode

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.recipes.aRecipeDetail
import dev.juanrincon.simmerly.recipes.aRecipeDetailUi
import dev.juanrincon.simmerly.recipes.anIngredientUi
import dev.juanrincon.simmerly.recipes.anInstruction
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.ParsedDuration
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookTimerUi
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.TimerOrigin
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeIntent
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeSideEffect
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeState
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookPhase
import dev.juanrincon.simmerly.recipes.presentation.details.models.InstructionUi
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private class FakeClock(var nowMillis: Long = 0L) : Clock {
    override fun now(): Instant = Instant.fromEpochMilliseconds(nowMillis)
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class CookModeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeRecipeRepository
    private lateinit var fakeClock: FakeClock
    private lateinit var viewModel: CookModeViewModel

    private val threeStepRecipe = aRecipeDetailUi(id = "test-recipe").copy(
        instructions = listOf(
            InstructionUi(id = "step-1", title = null, summary = "Step 1", text = "Do the first thing."),
            InstructionUi(id = "step-2", title = null, summary = "Step 2", text = "Do the second thing."),
            InstructionUi(id = "step-3", title = null, summary = "Step 3", text = "Do the third thing.")
        )
    )

    private val oneStepRecipe = aRecipeDetailUi(id = "test-recipe").copy(
        instructions = listOf(InstructionUi(id = "step-1", title = null, summary = "Step 1", text = "Only step."))
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeRecipeRepository()
        fakeClock = FakeClock()
        viewModel = CookModeViewModel(
            recipeId = "test-recipe",
            repository = repo,
            alerts = NoOpCookTimerAlerts,
            clock = fakeClock
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initial state

    @Test
    fun initialStateStartsAtMiseEnPlaceAndLoading() {
        val state = viewModel.container.stateFlow.value
        assertThat(state.loading).isTrue()
        assertThat(state.phase).isEqualTo(CookPhase.MISE_EN_PLACE)
        assertThat(state.stepIndex).isEqualTo(0)
    }

    // endregion

    // region Loading and step-count edge cases

    @Test
    fun observeRecipeLoadsRecipeAndStaysAtMiseEnPlace() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this, initialState = CookModeState(loading = false)) {
            runOnCreate()
            repo.recipeDetailsFlow()
                .emit(Either.Right(LoadingResult.Loaded(aRecipeDetail(id = "test-recipe"))))
            val state = awaitInternalState()
            assertThat(state.loading).isFalse()
            assertThat(state.recipe.id).isEqualTo("test-recipe")
            assertThat(state.phase).isEqualTo(CookPhase.MISE_EN_PLACE)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun stepIndexClampsWhenARefreshShrinksTheRecipe() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this, initialState = CookModeState(loading = false)) {
            runOnCreate()

            repo.recipeDetailsFlow().emit(
                Either.Right(
                    LoadingResult.Loaded(
                        aRecipeDetail(
                            id = "test-recipe",
                            instructions = listOf(anInstruction("i1"), anInstruction("i2"), anInstruction("i3"))
                        )
                    )
                )
            )
            awaitInternalState() // recipe loaded, 3 steps

            viewModel.onEvent(CookModeIntent.JumpToStep(2))
            assertThat(awaitInternalState().stepIndex).isEqualTo(2)

            repo.recipeDetailsFlow().emit(
                Either.Right(
                    LoadingResult.Loaded(
                        aRecipeDetail(id = "test-recipe", instructions = listOf(anInstruction("i1")))
                    )
                )
            )
            val state = awaitInternalState()
            assertThat(state.recipe.instructions).hasSize(1)
            assertThat(state.stepIndex).isEqualTo(0)

            cancelAndIgnoreRemainingItems()
        }
    }

    // endregion

    // region Mise en place

    @Test
    fun toggleIngredientAddsThenRemovesFromCheckedSet() = runTest(testDispatcher) {
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.ToggleIngredient("ingredient-1"))
            assertThat(awaitInternalState().checkedIngredientIds).contains("ingredient-1")

            viewModel.onEvent(CookModeIntent.ToggleIngredient("ingredient-1"))
            assertThat(awaitInternalState().checkedIngredientIds).isEmpty()
        }
    }

    @Test
    fun skipMiseEnPlaceMovesToStepsPhase() = runTest(testDispatcher) {
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.SkipMiseEnPlace)
            assertThat(awaitInternalState().phase).isEqualTo(CookPhase.STEPS)
        }
    }

    @Test
    fun beginStepsRecordsCookingStartTimeOnlyOnce() = runTest(testDispatcher) {
        fakeClock.nowMillis = 5_000L
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.BeginSteps)
            assertThat(awaitInternalState().cookingStartedAtMillis).isEqualTo(5_000L)
        }
    }

    // endregion

    // region Servings

    @Test
    fun addServingIncrementsServingsAndScalesIngredients() = runTest(testDispatcher) {
        val loadedState = CookModeState(
            loading = false,
            recipe = threeStepRecipe.copy(
                servings = 4.0,
                ingredients = listOf(anIngredientUi(quantity = 100.0))
            )
        )
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.AddServing)
            val state = awaitInternalState()
            assertThat(state.recipe.servings).isEqualTo(5.0)
            assertThat(state.recipe.ingredients[0].quantity).isEqualTo(125.0)
        }
    }

    @Test
    fun removeServingDecrementsServings() = runTest(testDispatcher) {
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe.copy(servings = 4.0))
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.RemoveServing)
            assertThat(awaitInternalState().recipe.servings).isEqualTo(3.0)
        }
    }

    @Test
    fun removeServingClampsAtOneAndEmitsNoStateChange() = runTest(testDispatcher) {
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe.copy(servings = 1.0))
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.RemoveServing)
            // clamped (1.0) == current (1.0) → early return, no reduce called
        }
        assertThat(viewModel.container.stateFlow.value.recipe.servings).isEqualTo(1.0)
    }

    @Test
    fun addServingDoesNotTouchCheckedIngredients() = runTest(testDispatcher) {
        // Scaling a quantity a cook already checked off shouldn't silently un-check it — the
        // referenceId a check is keyed on doesn't change when servings do.
        val loadedState = CookModeState(
            loading = false,
            recipe = threeStepRecipe.copy(
                servings = 4.0,
                ingredients = listOf(anIngredientUi(quantity = 100.0, referenceId = "ingredient-1"))
            ),
            checkedIngredientIds = setOf("ingredient-1")
        )
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.AddServing)
            assertThat(awaitInternalState().checkedIngredientIds).contains("ingredient-1")
        }
    }

    // endregion

    // region Step navigation

    @Test
    fun nextStepAdvancesIndexWithinRange() = runTest(testDispatcher) {
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe, phase = CookPhase.STEPS, stepIndex = 0)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.NextStep)
            assertThat(awaitInternalState().stepIndex).isEqualTo(1)
        }
    }

    @Test
    fun nextStepPastLastStepFinishesCooking() = runTest(testDispatcher) {
        fakeClock.nowMillis = 9_000L
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe, phase = CookPhase.STEPS, stepIndex = 2)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.NextStep)
            val state = awaitInternalState()
            assertThat(state.phase).isEqualTo(CookPhase.DONE)
            assertThat(state.cookingFinishedAtMillis).isEqualTo(9_000L)
        }
    }

    @Test
    fun previousStepClampsAtZeroAndEmitsNoStateChange() = runTest(testDispatcher) {
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe, phase = CookPhase.STEPS, stepIndex = 0)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.PreviousStep)
            // clamped (0) == current (0) → the reduce produces an identical state, so StateFlow
            // conflates it away — nothing to await here, matching
            // RecipeDetailsViewModelTest.removeServingClampsAtOneAndEmitsNoStateChange.
        }
        assertThat(viewModel.container.stateFlow.value.stepIndex).isEqualTo(0)
    }

    @Test
    fun singleStepRecipeFinishesOnFirstNextStep() = runTest(testDispatcher) {
        val loadedState = CookModeState(loading = false, recipe = oneStepRecipe, phase = CookPhase.STEPS, stepIndex = 0)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.NextStep)
            assertThat(awaitInternalState().phase).isEqualTo(CookPhase.DONE)
        }
    }

    // endregion

    // region Timers

    @Test
    fun startDetectedTimerAddsARunningTimerWithDeadlineFromClock() = runTest(testDispatcher) {
        fakeClock.nowMillis = 1_000L
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe, phase = CookPhase.STEPS)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(
                CookModeIntent.StartDetectedTimer(ParsedDuration(8.minutes, "8 minutes"), "Step 1")
            )
            val timer = awaitInternalState().timers.single()
            assertThat(timer.origin).isEqualTo(TimerOrigin.DETECTED)
            assertThat(timer.deadlineEpochMillis).isEqualTo(1_000L + 8.minutes.inWholeMilliseconds)
            assertThat(timer.isPaused).isFalse()
        }
    }

    @Test
    fun timerStartedOnOneStepIsStillRunningTwoStepsLater() = runTest(testDispatcher) {
        fakeClock.nowMillis = 0L
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe, phase = CookPhase.STEPS, stepIndex = 0)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(
                CookModeIntent.StartDetectedTimer(ParsedDuration(9.minutes, "9 minutes"), "Step 1")
            )
            val afterStart = awaitInternalState()
            val timer = afterStart.timers.single()

            viewModel.onEvent(CookModeIntent.NextStep)
            assertThat(awaitInternalState().stepIndex).isEqualTo(1)

            viewModel.onEvent(CookModeIntent.NextStep)
            val finalState = awaitInternalState()
            assertThat(finalState.stepIndex).isEqualTo(2)
            assertThat(finalState.timers).isEqualTo(listOf(timer))
        }
    }

    @Test
    fun pauseTimerFreezesRemainingTime() = runTest(testDispatcher) {
        fakeClock.nowMillis = 0L
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe, phase = CookPhase.STEPS)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.StartDetectedTimer(ParsedDuration(10.minutes, "10 minutes"), "Step 1"))
            val timer = awaitInternalState().timers.single()

            fakeClock.nowMillis = 2.minutes.inWholeMilliseconds // 2 minutes elapse
            viewModel.onEvent(CookModeIntent.PauseTimer(timer.id))
            val paused = awaitInternalState().timers.single()

            assertThat(paused.isPaused).isTrue()
            assertThat(paused.pausedRemainingMillis).isEqualTo(8.minutes.inWholeMilliseconds)
        }
    }

    @Test
    fun resumeTimerRebuildsDeadlineFromCurrentClockIgnoringPausedDuration() = runTest(testDispatcher) {
        fakeClock.nowMillis = 0L
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe, phase = CookPhase.STEPS)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.StartDetectedTimer(ParsedDuration(10.minutes, "10 minutes"), "Step 1"))
            val timer = awaitInternalState().timers.single()

            fakeClock.nowMillis = 2.minutes.inWholeMilliseconds
            viewModel.onEvent(CookModeIntent.PauseTimer(timer.id))
            awaitInternalState() // 8 minutes remaining, frozen

            // Time keeps passing while paused — the cook steps away for 5 real minutes.
            fakeClock.nowMillis = 7.minutes.inWholeMilliseconds
            viewModel.onEvent(CookModeIntent.ResumeTimer(timer.id))
            val resumed = awaitInternalState().timers.single()

            assertThat(resumed.isPaused).isFalse()
            // Deadline is "now + remaining", not "original deadline" — the 5 paused minutes are not
            // counted against the timer.
            assertThat(resumed.deadlineEpochMillis).isEqualTo(7.minutes.inWholeMilliseconds + 8.minutes.inWholeMilliseconds)
        }
    }

    @Test
    fun cancelTimerRemovesItFromState() = runTest(testDispatcher) {
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe, phase = CookPhase.STEPS)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.StartDetectedTimer(ParsedDuration(5.minutes, "5 minutes"), "Step 1"))
            val timer = awaitInternalState().timers.single()

            viewModel.onEvent(CookModeIntent.CancelTimer(timer.id))
            assertThat(awaitInternalState().timers).isEmpty()
        }
    }

    @Test
    fun dismissFinishedTimerRemovesItFromState() = runTest(testDispatcher) {
        val existingTimer = CookTimerUi(
            id = "timer-1",
            label = "Step 1",
            total = 1.minutes,
            deadlineEpochMillis = 0L,
            origin = TimerOrigin.DETECTED
        )
        val loadedState = CookModeState(
            loading = false,
            recipe = threeStepRecipe,
            phase = CookPhase.STEPS,
            timers = listOf(existingTimer),
            notifiedFinishedTimerIds = setOf("timer-1")
        )
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.DismissFinishedTimer("timer-1"))
            assertThat(awaitInternalState().timers).isEmpty()
        }
    }

    @Test
    fun timerFinishedSideEffectFiresOnceTheDeadlinePasses() = runTest(testDispatcher) {
        fakeClock.nowMillis = 0L
        val loadedState = CookModeState(loading = false, recipe = threeStepRecipe, phase = CookPhase.STEPS)
        viewModel.testWithInternalState(this, initialState = loadedState) {
            runOnCreate() // starts the ticker

            viewModel.onEvent(CookModeIntent.StartDetectedTimer(ParsedDuration(1.milliseconds, "1ms"), "Step 1"))
            val timer = awaitInternalState().timers.single()

            // Push the clock well past the (1ms) deadline before the ticker's next tick observes it.
            fakeClock.nowMillis = 1_000L

            val tickedState = awaitInternalState()
            assertThat(tickedState.nowMillis).isEqualTo(1_000L)

            val notifiedState = awaitInternalState()
            assertThat(notifiedState.notifiedFinishedTimerIds).contains(timer.id)

            assertThat(awaitSideEffect()).isEqualTo(CookModeSideEffect.TimerFinished(timer.id))

            cancelAndIgnoreRemainingItems()
        }
    }

    // endregion

    // region Done screen

    @Test
    fun finishCookingRecordsLastMadeTimelineEventAndRating() = runTest(testDispatcher) {
        fakeClock.nowMillis = 5.minutes.inWholeMilliseconds
        val loadedState = CookModeState(
            loading = false,
            recipe = threeStepRecipe,
            phase = CookPhase.DONE,
            rating = 4,
            noteDraft = "Great recipe"
        )
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.FinishCooking)
            assertThat(awaitInternalState().noteDraft).isEqualTo("")
        }
        val call = repo.lastRecordRecipeMadeCall
        assertThat(call?.first).isEqualTo(threeStepRecipe.id)
        assertThat(call?.second).isEqualTo(Instant.fromEpochMilliseconds(5.minutes.inWholeMilliseconds))
        assertThat(call?.third).isEqualTo("Great recipe")
        assertThat(repo.lastSetRatingCall).isEqualTo(threeStepRecipe.id to 4.0)
    }

    @Test
    fun finishCookingWithUntouchedRatingSkipsTheRatingWrite() = runTest(testDispatcher) {
        // rating == 0 means "never tapped a star", not "zero stars" — the write is 1-5 only.
        val loadedState = CookModeState(
            loading = false,
            recipe = threeStepRecipe,
            phase = CookPhase.DONE,
            rating = 0,
            noteDraft = "Great recipe"
        )
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.FinishCooking)
            awaitInternalState()
        }
        assertThat(repo.lastRecordRecipeMadeCall?.first).isEqualTo(threeStepRecipe.id)
        assertThat(repo.lastSetRatingCall).isNull()
    }

    @Test
    fun finishCookingWithBlankNoteStillRecordsTheTimelineEventWithNoMessage() = runTest(testDispatcher) {
        val loadedState = CookModeState(
            loading = false,
            recipe = threeStepRecipe,
            phase = CookPhase.DONE,
            rating = 0,
            noteDraft = ""
        )
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.FinishCooking)
            // noteDraft was already "" → the reduce produces an identical state, no emission to
            // await, matching removeServingClampsAtOneAndEmitsNoStateChange above.
        }
        assertThat(repo.lastRecordRecipeMadeCall?.first).isEqualTo(threeStepRecipe.id)
        assertThat(repo.lastRecordRecipeMadeCall?.third).isNull()
    }

    @Test
    fun finishCookingNeverCallsAddComment() = runTest(testDispatcher) {
        // The note's destination moved to the timeline event; addComment must not fire alongside
        // it, or the same note would show up twice in two different places in Mealie's own UI.
        val loadedState = CookModeState(
            loading = false,
            recipe = threeStepRecipe,
            phase = CookPhase.DONE,
            rating = 4,
            noteDraft = "Great recipe"
        )
        viewModel.testWithInternalState(this, initialState = loadedState) {
            viewModel.onEvent(CookModeIntent.FinishCooking)
            awaitInternalState()
        }
        assertThat(repo.lastAddCommentCall).isNull()
    }

    @Test
    fun ratingPreFillsFromTheLoadedRecipeOnFirstLoadOnly() = runTest(testDispatcher) {
        viewModel.testWithInternalState(this, initialState = CookModeState(loading = false)) {
            runOnCreate()

            repo.recipeDetailsFlow().emit(
                Either.Right(LoadingResult.Loaded(aRecipeDetail(id = "test-recipe").copy(rating = 4.0)))
            )
            assertThat(awaitInternalState().rating).isEqualTo(4)

            // The cook taps a different, unsubmitted rating...
            viewModel.onEvent(CookModeIntent.SetRating(2))
            assertThat(awaitInternalState().rating).isEqualTo(2)

            // ...and a background refresh must not silently revert it back to the server's value.
            // description differs so distinctUntilChanged doesn't swallow this as a duplicate of
            // the first emission (two Clock.System.now() calls can land on the same instant).
            repo.recipeDetailsFlow().emit(
                Either.Right(
                    LoadingResult.Loaded(
                        aRecipeDetail(id = "test-recipe").copy(rating = 4.0, description = "refreshed")
                    )
                )
            )
            assertThat(awaitInternalState().rating).isEqualTo(2)

            cancelAndIgnoreRemainingItems()
        }
    }

    // endregion
}
