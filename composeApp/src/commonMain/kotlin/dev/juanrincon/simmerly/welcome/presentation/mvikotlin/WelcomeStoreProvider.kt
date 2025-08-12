package dev.juanrincon.simmerly.welcome.presentation.mvikotlin

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStoreProvider.Message.*

class WelcomeStoreProvider(
    private val storeFactory: StoreFactory,
) {
    fun provide(): WelcomeStore = object : WelcomeStore, Store<WelcomeStore.Intent, WelcomeStore.State, Nothing> by storeFactory.create(
        name = "WelcomeStore",
        initialState = WelcomeStore.State(),
        bootstrapper = null,
        executorFactory = ::WelcomeExecutorImpl,
        reducer = WelcomeReducerImpl
    ) {}

    private sealed interface Message {
        data class ServerAddressChanged(val serverAddress: String) : Message
        data class LoginTypeChanged(val loginType: WelcomeStore.LoginType) : Message
        data class UsernameChanged(val username: String) : Message
        data class PasswordChanged(val password: String) : Message
        data class ApiKeyChanged(val apiKey: String) : Message
    }

    private inner class WelcomeExecutorImpl : CoroutineExecutor<WelcomeStore.Intent, Unit, WelcomeStore.State, Message, Nothing>() {

        override fun executeIntent(intent: WelcomeStore.Intent) = when (intent) {
            is WelcomeStore.Intent.OnServerAddressChanged -> dispatch(ServerAddressChanged(intent.serverAddress))
            is WelcomeStore.Intent.OnApiKeyChanged -> dispatch(ApiKeyChanged(intent.apiKey))
            is WelcomeStore.Intent.OnLoginTypeChanged -> dispatch(LoginTypeChanged(intent.type))
            WelcomeStore.Intent.OnLoginClicked -> TODO()
            is WelcomeStore.Intent.OnPasswordChanged -> dispatch(PasswordChanged(intent.password))
            is WelcomeStore.Intent.OnUsernameChanged -> dispatch(UsernameChanged(intent.username))
        }
    }

    private object WelcomeReducerImpl : Reducer<WelcomeStore.State, Message> {
        override fun WelcomeStore.State.reduce(
            msg: Message
        ): WelcomeStore.State = when (msg) {
            is ServerAddressChanged -> copy(serverAddress = msg.serverAddress)
            is ApiKeyChanged -> copy(apiKey = msg.apiKey)
            is PasswordChanged -> copy(password = msg.password)
            is UsernameChanged -> copy(username = msg.username)
            is LoginTypeChanged -> copy(loginType = msg.loginType)
        }

    }
}