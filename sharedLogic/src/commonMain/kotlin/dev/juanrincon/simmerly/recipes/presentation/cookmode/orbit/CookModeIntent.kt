package dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit

import dev.juanrincon.simmerly.recipes.domain.ParsedDuration

sealed interface CookModeIntent {
    data class ToggleIngredient(val referenceId: String) : CookModeIntent
    data object SkipMiseEnPlace : CookModeIntent
    data object BeginSteps : CookModeIntent

    data object NextStep : CookModeIntent
    data object PreviousStep : CookModeIntent
    data class JumpToStep(val index: Int) : CookModeIntent

    data class StartDetectedTimer(val duration: ParsedDuration, val label: String) : CookModeIntent
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
    data object SubmitNote : CookModeIntent

    data object Exit : CookModeIntent
}
