package dev.juanrincon.simmerly.welcome.presentation.orbit

import dev.juanrincon.simmerly.welcome.presentation.model.CredentialType

sealed interface WelcomeIntent {
    data class OnCredentialTypeChanged(val credentialType: CredentialType) : WelcomeIntent
    data class OnServerAddressChanged(val value: String) : WelcomeIntent
    data class OnUsernameChanged(val value: String) : WelcomeIntent
    data class OnPasswordChanged(val value: String) : WelcomeIntent
    data object OnLoginClicked : WelcomeIntent
}
