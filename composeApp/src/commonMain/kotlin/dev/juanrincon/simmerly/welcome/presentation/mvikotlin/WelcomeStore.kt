package dev.juanrincon.simmerly.welcome.presentation.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore.Intent
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore.State

interface WelcomeStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        data class OnServerAddressChanged(val serverAddress: String) : Intent
        data class OnUsernameChanged(val username: String) : Intent
        data class OnPasswordChanged(val password: String) : Intent
        object OnLoginClicked : Intent
    }

    data class State(
        val serverAddress: String = "",
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val isLoginButtonEnabled: Boolean = false
    )
}
