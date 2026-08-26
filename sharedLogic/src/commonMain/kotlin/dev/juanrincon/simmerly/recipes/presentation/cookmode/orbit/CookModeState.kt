package dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit

import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.domain.suggestedTimerOptions
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookTimerUi
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.toCookSteps
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import kotlin.time.Duration

enum class CookPhase { MISE_EN_PLACE, STEPS, DONE }

data class NewTimerDraft(
    val minutes: Int = 5,
    val seconds: Int = 0,
    val label: String = ""
)

data class CookModeState(
    val loading: Boolean = true,
    val recipe: RecipeDetailUi = RecipeDetailUi.emptyRecipe,
    val error: RecipesError? = null,
    val phase: CookPhase = CookPhase.MISE_EN_PLACE,
    val stepIndex: Int = 0,
    val checkedIngredientIds: Set<String> = emptySet(),
    val timers: List<CookTimerUi> = emptyList(),
    val notifiedFinishedTimerIds: Set<String> = emptySet(),
    val nowMillis: Long = 0L,
    val cookingStartedAtMillis: Long? = null,
    val cookingFinishedAtMillis: Long? = null,
    val showTimerList: Boolean = false,
    val newTimerDraft: NewTimerDraft? = null,
    /** Which option the cook picked from the current step's detected time range. Null means
     * "untouched", which resolves to the shortest option — see [selectedRangeOptionOrDefault].
     * Cleared whenever the step changes, since the options belong to that step. */
    val selectedRangeOption: Duration? = null,
    val rating: Int = 0,
    val noteDraft: String = ""
) {
    val steps get() = recipe.toCookSteps()

    val currentStep get() = steps.getOrNull(stepIndex)

    /** The timer options offered for the current step's first detected duration. A single
     * detected duration yields one option; a range yields a handful spanning it. */
    val currentStepTimerOptions: List<Duration>
        get() = currentStep?.detectedDurations?.firstOrNull()?.suggestedTimerOptions().orEmpty()

    /** The option the Start button will actually use: whatever the cook tapped, or the shortest
     * on offer. The design preselects the short end — start short, taste, add a minute. */
    val selectedRangeOptionOrDefault: Duration?
        get() = currentStepTimerOptions.let { options ->
            selectedRangeOption?.takeIf { it in options } ?: options.firstOrNull()
        }

    val readyIngredientCount get() = recipe.ingredients.count { it.referenceId in checkedIngredientIds }

    /** [currentStepTimerOptions] in milliseconds. `Duration` bridges to Swift as an opaque raw
     * Long (Kotlin's internal encoding, not a millisecond count), so Swift can't do arithmetic on
     * it directly — this is the list it actually wants. */
    val currentStepTimerOptionsMillis: List<Long>
        get() = currentStepTimerOptions.map { it.inWholeMilliseconds }

    /** [selectedRangeOptionOrDefault] in milliseconds — see [currentStepTimerOptionsMillis] for
     * why this exists. */
    val selectedRangeOptionMillis: Long?
        get() = selectedRangeOptionOrDefault?.inWholeMilliseconds

    /** This state with [nowMillis] zeroed out, so Swift can diff two states while ignoring the
     * 4Hz ticker field — see [dev.juanrincon.simmerly.recipes.presentation.cookmode.CookModeViewModel]'s
     * `runTicker`. Rendering off `==` on the full state would rebuild the whole screen on every
     * tick instead of just the timer countdowns. */
    fun withoutNow(): CookModeState = copy(nowMillis = 0L)
}
