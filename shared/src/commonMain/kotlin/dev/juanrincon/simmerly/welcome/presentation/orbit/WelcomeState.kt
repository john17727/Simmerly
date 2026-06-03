package dev.juanrincon.simmerly.welcome.presentation.orbit

import androidx.compose.foundation.text.input.TextFieldState
import dev.juanrincon.simmerly.welcome.presentation.model.CredentialType

data class WelcomeState(
    val serverAddress: TextFieldState = TextFieldState(""),
    val credentialType: CredentialType = CredentialType.CREDENTIALS,
    val username: TextFieldState = TextFieldState(""),
    val password: TextFieldState = TextFieldState(""),
    val isLoading: Boolean = false,
) {
    val isLoginButtonEnabled: Boolean
        get() = when (credentialType) {
            CredentialType.CREDENTIALS -> serverAddress.text.isNotBlank() && username.text.isNotBlank() && password.text.isNotBlank()
            CredentialType.API_TOKEN -> serverAddress.text.isNotBlank() && password.text.isNotBlank()
        }
}
