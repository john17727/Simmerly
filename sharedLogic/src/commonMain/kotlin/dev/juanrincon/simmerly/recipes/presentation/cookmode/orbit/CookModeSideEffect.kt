package dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit

sealed interface CookModeSideEffect {
    data object Exit : CookModeSideEffect
    data class TimerFinished(val timerId: String) : CookModeSideEffect
}
