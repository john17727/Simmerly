package dev.juanrincon.simmerly.welcome.presentation.orbit

import org.jetbrains.compose.resources.StringResource

sealed interface WelcomeSideEffect {
    data class LoginFailed(val message: StringResource) : WelcomeSideEffect
}