package dev.juanrincon.simmerly.recipes.presentation.cookmode

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookTimerUi
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.TimerOrigin
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeIntent
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeSideEffect
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeState
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookPhase
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.NewTimerDraft
import dev.juanrincon.simmerly.recipes.presentation.details.mappers.toRecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.withServings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CookModeViewModel(
    private val recipeId: String,
    private val repository: RecipeRepository,
    private val alerts: CookTimerAlerts = NoOpCookTimerAlerts,
    private val clock: Clock = Clock.System,
) : OrbitContainerHost<CookModeState, CookModeState, CookModeSideEffect>, ViewModel() {

    override val container: OrbitContainer<CookModeState, CookModeState, CookModeSideEffect> =
        orbitContainer(initialState = CookModeState(nowMillis = clock.now().toEpochMilliseconds())) {
            observeRecipe()
            runTicker()
        }

    // Concrete, non-generic accessors for Swift — see WelcomeViewModel for why these are needed.
    val stateFlow: StateFlow<CookModeState> get() = container.stateFlow
    val sideEffectFlow: Flow<CookModeSideEffect> get() = container.sideEffectFlow

    fun onEvent(event: CookModeIntent) {
        when (event) {
            is CookModeIntent.ToggleIngredient -> toggleIngredient(event.referenceId)
            CookModeIntent.AddServing -> updateServing(1)
            CookModeIntent.RemoveServing -> updateServing(-1)
            CookModeIntent.SkipMiseEnPlace -> beginSteps()
            CookModeIntent.BeginSteps -> beginSteps()

            CookModeIntent.NextStep -> advanceStep(1)
            CookModeIntent.PreviousStep -> advanceStep(-1)
            is CookModeIntent.JumpToStep -> jumpToStep(event.index)

            is CookModeIntent.StartDetectedTimer -> startDetectedTimer(event.duration.duration, event.label)
            is CookModeIntent.SelectRangeOption -> intent {
                reduce { state.copy(selectedRangeOption = event.duration) }
            }
            is CookModeIntent.StartSelectedRangeTimer -> intent {
                val selected = state.selectedRangeOptionOrDefault ?: return@intent
                startDetectedTimer(selected, event.label)
            }
            CookModeIntent.ShowNewTimerSheet -> showNewTimerSheet()
            is CookModeIntent.UpdateTimerDraft -> intent {
                reduce { state.copy(newTimerDraft = event.draft) }
            }
            CookModeIntent.ConfirmNewTimer -> confirmNewTimer()
            CookModeIntent.DismissNewTimerSheet -> intent {
                reduce { state.copy(newTimerDraft = null) }
            }
            is CookModeIntent.PauseTimer -> pauseTimer(event.id)
            is CookModeIntent.ResumeTimer -> resumeTimer(event.id)
            is CookModeIntent.CancelTimer -> cancelTimer(event.id)
            is CookModeIntent.DismissFinishedTimer -> cancelTimer(event.id)

            CookModeIntent.ShowTimerList -> intent { reduce { state.copy(showTimerList = true) } }
            CookModeIntent.DismissTimerList -> intent { reduce { state.copy(showTimerList = false) } }

            is CookModeIntent.SetRating -> intent { reduce { state.copy(rating = event.rating) } }
            is CookModeIntent.UpdateNote -> intent { reduce { state.copy(noteDraft = event.text) } }
            CookModeIntent.FinishCooking -> finishCooking()

            CookModeIntent.Exit -> intent { postSideEffect(CookModeSideEffect.Exit) }
        }
    }

    private fun observeRecipe() = intent {
        repository.recipeDetails(recipeId)
            .distinctUntilChanged()
            .collect { response ->
                response.fold(
                    ifLeft = { error ->
                        if (state.recipe == RecipeDetailUi.emptyRecipe) {
                            reduce { state.copy(loading = false, error = error) }
                        }
                    },
                    ifRight = { result ->
                        when (result) {
                            LoadingResult.Loading -> reduce { state.copy(loading = true) }
                            LoadingResult.Refreshing, LoadingResult.RefreshComplete -> Unit
                            is LoadingResult.Loaded -> {
                                val recipe = result.data.toRecipeDetailUi()
                                reduce {
                                    // A background refresh can change the instruction count out from
                                    // under a cook mid-flow — never leave stepIndex pointing past the end.
                                    val clampedIndex = if (recipe.instructions.isEmpty()) {
                                        0
                                    } else {
                                        state.stepIndex.coerceIn(0, recipe.instructions.lastIndex)
                                    }
                                    // Pre-fill the Done screen's stars from the recipe's existing
                                    // rating, but only on the very first load — a background
                                    // refresh mid-cook must never stomp a star the user already
                                    // tapped and hasn't submitted yet.
                                    val isFirstLoad = state.recipe == RecipeDetailUi.emptyRecipe
                                    state.copy(
                                        loading = false,
                                        error = null,
                                        recipe = recipe,
                                        stepIndex = clampedIndex,
                                        rating = if (isFirstLoad) {
                                            recipe.rating?.roundToInt() ?: 0
                                        } else {
                                            state.rating
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
    }

    /**
     * Only spins while a timer is actually running, so an idle Cook Mode session (no timers, or
     * every timer paused) costs nothing. Timers themselves are deadline-based — this ticker just
     * refreshes [CookModeState.nowMillis] often enough for a smooth countdown display and to catch
     * the tick a deadline is crossed.
     */
    private fun runTicker() = intent {
        while (true) {
            val hasRunningTimer = state.timers.any { !it.isPaused }
            if (hasRunningTimer) {
                val now = clock.now().toEpochMilliseconds()
                reduce { state.copy(nowMillis = now) }

                val newlyFinished = state.timers.filter {
                    it.isFinished(now) && it.id !in state.notifiedFinishedTimerIds
                }
                if (newlyFinished.isNotEmpty()) {
                    reduce {
                        state.copy(
                            notifiedFinishedTimerIds = state.notifiedFinishedTimerIds + newlyFinished.map { it.id }
                        )
                    }
                    newlyFinished.forEach { postSideEffect(CookModeSideEffect.TimerFinished(it.id)) }
                }
                delay(TICK_INTERVAL)
            } else {
                // Still refresh the clock with no timer running: the desktop console shows a live
                // elapsed-cooking readout, and leaving nowMillis frozen would stop it dead the
                // moment the last timer was cancelled. Half a second is ample for an m:ss label.
                reduce { state.copy(nowMillis = clock.now().toEpochMilliseconds()) }
                delay(IDLE_POLL_INTERVAL)
            }
        }
    }

    private fun toggleIngredient(referenceId: String) = intent {
        reduce {
            val checked = state.checkedIngredientIds
            state.copy(
                checkedIngredientIds = if (referenceId in checked) {
                    checked - referenceId
                } else {
                    checked + referenceId
                }
            )
        }
    }

    private fun updateServing(delta: Int) = intent {
        val current = state.recipe
        if (current == RecipeDetailUi.emptyRecipe) return@intent

        val updated = current.withServings(current.servings + delta)
        if (updated == current) return@intent

        reduce { state.copy(recipe = updated) }
    }

    private fun beginSteps() = intent {
        reduce {
            state.copy(
                phase = CookPhase.STEPS,
                cookingStartedAtMillis = state.cookingStartedAtMillis ?: clock.now().toEpochMilliseconds()
            )
        }
    }

    private fun advanceStep(delta: Int) = intent {
        val steps = state.steps
        if (steps.isEmpty()) return@intent
        val nextIndex = state.stepIndex + delta
        if (nextIndex > steps.lastIndex) {
            reduce {
                state.copy(phase = CookPhase.DONE, cookingFinishedAtMillis = clock.now().toEpochMilliseconds())
            }
        } else {
            // selectedRangeOption belongs to the step it was picked on — drop it on the way out.
            reduce {
                state.copy(
                    stepIndex = nextIndex.coerceIn(0, steps.lastIndex),
                    selectedRangeOption = null
                )
            }
        }
    }

    private fun jumpToStep(index: Int) = intent {
        val steps = state.steps
        if (steps.isEmpty()) return@intent
        reduce {
            state.copy(
                stepIndex = index.coerceIn(0, steps.lastIndex),
                showTimerList = false,
                selectedRangeOption = null
            )
        }
    }

    private fun startDetectedTimer(duration: Duration, label: String) = intent {
        val timer = CookTimerUi(
            id = newTimerId(),
            label = label,
            total = duration,
            deadlineEpochMillis = clock.now().toEpochMilliseconds() + duration.inWholeMilliseconds,
            origin = TimerOrigin.DETECTED
        )
        reduce { state.copy(timers = state.timers + timer) }
        alerts.schedule(timer)
    }

    private fun showNewTimerSheet() = intent {
        reduce {
            state.copy(
                newTimerDraft = NewTimerDraft(label = state.currentStep?.instruction?.summary.orEmpty())
            )
        }
    }

    private fun confirmNewTimer() = intent {
        val draft = state.newTimerDraft ?: return@intent
        val total = draft.minutes.minutes + draft.seconds.seconds
        if (total <= Duration.ZERO) return@intent

        val timer = CookTimerUi(
            id = newTimerId(),
            label = draft.label.ifBlank { "Timer" },
            total = total,
            deadlineEpochMillis = clock.now().toEpochMilliseconds() + total.inWholeMilliseconds,
            origin = TimerOrigin.USER
        )
        reduce { state.copy(newTimerDraft = null, timers = state.timers + timer) }
        alerts.schedule(timer)
    }

    private fun pauseTimer(id: String) = intent {
        val now = clock.now().toEpochMilliseconds()
        reduce {
            state.copy(
                timers = state.timers.map { timer ->
                    if (timer.id == id && !timer.isPaused) {
                        timer.copy(
                            deadlineEpochMillis = null,
                            pausedRemainingMillis = timer.remaining(now).inWholeMilliseconds
                        )
                    } else {
                        timer
                    }
                }
            )
        }
        // A scheduled alert (e.g. iOS's local notification) is armed for the deadline that just
        // stopped applying — leaving it pending would fire a notification for a timer that isn't
        // even running anymore. resumeTimer() re-schedules against the new deadline.
        alerts.cancel(id)
    }

    private fun resumeTimer(id: String) = intent {
        val now = clock.now().toEpochMilliseconds()
        reduce {
            state.copy(
                timers = state.timers.map { timer ->
                    if (timer.id == id && timer.isPaused) {
                        val remainingMillis = timer.pausedRemainingMillis ?: timer.total.inWholeMilliseconds
                        timer.copy(deadlineEpochMillis = now + remainingMillis, pausedRemainingMillis = null)
                    } else {
                        timer
                    }
                }
            )
        }
        state.timers.firstOrNull { it.id == id }?.let { alerts.schedule(it) }
    }

    private fun cancelTimer(id: String) = intent {
        reduce {
            state.copy(
                timers = state.timers.filterNot { it.id == id },
                notifiedFinishedTimerIds = state.notifiedFinishedTimerIds - id
            )
        }
        alerts.cancel(id)
    }

    /**
     * The Done button. Two independent writes — a failure in one must not block the other:
     * "the recipe was made" (last-made + its timeline entry, one repository operation — they
     * always fire together) happens unconditionally, matching Mealie's own semantics of recording
     * a cook regardless of whether it's rated; the rating fires only when the cook actually tapped
     * a star. [CookModeState.rating] defaults to 0, which is "untouched", not "zero stars" — a
     * real rating write is always 1-5.
     */
    private fun finishCooking() = intent {
        val recipeId = state.recipe.id
        val note = state.noteDraft.ifBlank { null }
        val rating = state.rating

        // TODO: surface a failure in either of these instead of swallowing it silently. Both
        // self-correct on the next recipeDetails emission if they landed anyway.
        repository.recordRecipeMade(recipeId, clock.now(), note)
        if (rating > 0) {
            repository.setRating(recipeId, rating.toDouble())
        }

        reduce { state.copy(noteDraft = "") }
    }

    private fun newTimerId(): String {
        val now = clock.now()
        return "timer-${now.epochSeconds}-${now.nanosecondsOfSecond}"
    }

    private companion object {
        val TICK_INTERVAL = 250.milliseconds
        val IDLE_POLL_INTERVAL = 500.milliseconds
    }
}
