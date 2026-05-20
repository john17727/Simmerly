package dev.juanrincon.simmerly.welcome.presentation.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore.Intent
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore.Label
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore.State
import org.jetbrains.compose.resources.StringResource

interface WelcomeStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class OnServerAddressChanged(val serverAddress: String) : Intent
        data class OnCredentialTypeChanged(val credentialType: CredentialType) : Intent
        data class OnUsernameChanged(val username: String) : Intent
        data class OnPasswordChanged(val password: String) : Intent
        object OnLoginClicked : Intent
    }

    data class State(
        val serverAddress: String = "",
        val credentialType: CredentialType = CredentialType.CREDENTIALS,
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val isLoginButtonEnabled: Boolean = false
    )

    sealed interface Label {
        data class LoginFailed(val message: StringResource) : Label
    }

    enum class CredentialType {
        CREDENTIALS,
        API_TOKEN
    }
}
