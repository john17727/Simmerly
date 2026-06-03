package dev.juanrincon.simmerly.welcome.presentation.orbit

import dev.juanrincon.simmerly.welcome.presentation.model.CredentialType

sealed interface WelcomeIntent {
    data class OnCredentialTypeChanged(val credentialType: CredentialType) : WelcomeIntent
    data object OnLoginClicked : WelcomeIntent
}
