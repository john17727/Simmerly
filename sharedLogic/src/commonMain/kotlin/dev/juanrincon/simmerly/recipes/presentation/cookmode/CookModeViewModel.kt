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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
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
            CookModeIntent.SkipMiseEnPlace -> beginSteps()
            CookModeIntent.BeginSteps -> beginSteps()

            CookModeIntent.NextStep -> advanceStep(1)
            CookModeIntent.PreviousStep -> advanceStep(-1)
            is CookModeIntent.JumpToStep -> jumpToStep(event.index)

            is CookModeIntent.StartDetectedTimer -> startDetectedTimer(event.duration.duration, event.label)
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
            CookModeIntent.SubmitNote -> submitNote()

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
                                    state.copy(
                                        loading = false,
                                        error = null,
                                        recipe = recipe,
                                        stepIndex = clampedIndex
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
            reduce { state.copy(stepIndex = nextIndex.coerceIn(0, steps.lastIndex)) }
        }
    }

    private fun jumpToStep(index: Int) = intent {
        val steps = state.steps
        if (steps.isEmpty()) return@intent
        reduce { state.copy(stepIndex = index.coerceIn(0, steps.lastIndex), showTimerList = false) }
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

    private fun submitNote() = intent {
        val note = state.noteDraft
        if (note.isBlank()) return@intent
        repository.addComment(state.recipe.id, note).fold(
            ifRight = { reduce { state.copy(noteDraft = "") } },
            ifLeft = { /* TODO: surface note submission failure */ }
        )
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
