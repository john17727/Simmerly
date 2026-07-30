package dev.juanrincon.simmerly.welcome.presentation.orbit

import dev.juanrincon.simmerly.core.presentation.UiText

sealed interface WelcomeSideEffect {
    data class LoginFailed(val message: UiText) : WelcomeSideEffect
}
