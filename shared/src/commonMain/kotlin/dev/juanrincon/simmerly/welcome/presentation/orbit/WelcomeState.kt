package dev.juanrincon.simmerly.welcome.presentation.orbit

import dev.juanrincon.simmerly.welcome.presentation.model.CredentialType

data class WelcomeState(
    val serverAddress: String = "",
    val credentialType: CredentialType = CredentialType.CREDENTIALS,
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
) {
    val isLoginButtonEnabled: Boolean
        get() = when (credentialType) {
            CredentialType.CREDENTIALS -> serverAddress.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            CredentialType.API_TOKEN -> serverAddress.isNotBlank() && password.isNotBlank()
        }
}
