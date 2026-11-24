package dev.juanrincon.simmerly.timeline.presentation.orbit

import dev.juanrincon.simmerly.core.presentation.UiText

sealed interface TimelineEditSideEffect {
    data class ShowMessage(val message: UiText) : TimelineEditSideEffect

    /** Saved or deleted: the screen should close. */
    data object Finished : TimelineEditSideEffect
}
