package dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit

import dev.juanrincon.simmerly.recipes.domain.ParsedDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

sealed interface CookModeIntent {
    data class ToggleIngredient(val referenceId: String) : CookModeIntent
    data object AddServing : CookModeIntent
    data object RemoveServing : CookModeIntent
    data object SkipMiseEnPlace : CookModeIntent
    data object BeginSteps : CookModeIntent

    data object NextStep : CookModeIntent
    data object PreviousStep : CookModeIntent
    data class JumpToStep(val index: Int) : CookModeIntent

    data class StartDetectedTimer(val duration: ParsedDuration, val label: String) : CookModeIntent

    /** Picks one option out of a detected time range's suggestions, without starting it yet. */
    data class SelectRangeOption(val duration: Duration) : CookModeIntent

    /** Starts a timer for the option currently selected on this step's detected range. */
    data class StartSelectedRangeTimer(val label: String) : CookModeIntent
    data object ShowNewTimerSheet : CookModeIntent
    data class UpdateTimerDraft(val draft: NewTimerDraft) : CookModeIntent
    data object ConfirmNewTimer : CookModeIntent
    data object DismissNewTimerSheet : CookModeIntent
    data class PauseTimer(val id: String) : CookModeIntent
    data class ResumeTimer(val id: String) : CookModeIntent
    data class CancelTimer(val id: String) : CookModeIntent
    data class DismissFinishedTimer(val id: String) : CookModeIntent
    data object ShowTimerList : CookModeIntent
    data object DismissTimerList : CookModeIntent

    data class SetRating(val rating: Int) : CookModeIntent
    data class UpdateNote(val text: String) : CookModeIntent

    /** The Done button: records last-made and a "Cooked" timeline event (the note as its
     * message, if any) unconditionally, and the rating only if one was actually tapped. */
    data object FinishCooking : CookModeIntent

    data object Exit : CookModeIntent
}

/** [CookModeIntent.SelectRangeOption] takes a `Duration`, which bridges to Swift as an opaque raw
 * Long (Kotlin's internal encoding, not a millisecond count) that Swift can't construct
 * arithmetic on. iOS selects by milliseconds instead — exact, since suggested options
 * ([CookModeState.currentStepTimerOptionsMillis]) are always whole seconds or minutes. */
fun selectRangeOptionMillis(millis: Long): CookModeIntent =
    CookModeIntent.SelectRangeOption(millis.milliseconds)
