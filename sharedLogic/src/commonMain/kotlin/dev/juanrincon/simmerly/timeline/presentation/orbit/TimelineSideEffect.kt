package dev.juanrincon.simmerly.timeline.presentation.orbit

import dev.juanrincon.simmerly.core.presentation.UiText

sealed interface TimelineSideEffect {
    data class ShowMessage(val message: UiText) : TimelineSideEffect
}
