package dev.juanrincon.simmerly.welcome.presentation.mvikotlin

import app.tracktion.core.domain.util.onError
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStoreFactory.Message.LoadingStateChanged
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStoreFactory.Message.PasswordChanged
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStoreFactory.Message.ServerAddressChanged
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStoreFactory.Message.UsernameChanged
import kotlinx.coroutines.launch
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.login_failed
import simmerly.composeapp.generated.resources.something_went_wrong
import simmerly.composeapp.generated.resources.unreachable_server_address

class WelcomeStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository
) {
    fun create(): WelcomeStore = object : WelcomeStore,
        Store<WelcomeStore.Intent, WelcomeStore.State, WelcomeStore.Label> by storeFactory.create(
            name = "WelcomeStore",
            initialState = WelcomeStore.State(),
            bootstrapper = null,
            executorFactory = ::WelcomeExecutorImpl,
            reducer = WelcomeReducerImpl
        ) {}

    private sealed interface Message {
        data class ServerAddressChanged(val serverAddress: String) : Message

        data class CredentialTypeChanged(val credentialType: WelcomeStore.CredentialType) : Message

        data class UsernameChanged(val username: String) : Message

        data class PasswordChanged(val password: String) : Message

        data class LoadingStateChanged(val isLoading: Boolean) : Message
    }

    private inner class WelcomeExecutorImpl :
        CoroutineExecutor<WelcomeStore.Intent, Unit, WelcomeStore.State, Message, WelcomeStore.Label>() {

        override fun executeIntent(intent: WelcomeStore.Intent) = when (intent) {
            is WelcomeStore.Intent.OnServerAddressChanged -> {
                dispatch(ServerAddressChanged(intent.serverAddress))
            }

            WelcomeStore.Intent.OnLoginClicked -> logInUser(
                state().serverAddress,
                state().username,
                state().password,
            )

            is WelcomeStore.Intent.OnPasswordChanged -> {
                dispatch(PasswordChanged(intent.password))
            }

            is WelcomeStore.Intent.OnUsernameChanged -> {
                dispatch(UsernameChanged(intent.username))
            }

            is WelcomeStore.Intent.OnCredentialTypeChanged -> {
                dispatch(Message.CredentialTypeChanged(intent.credentialType))
            }
        }

        private fun logInUser(serverAddress: String, username: String, password: String) {
            if (state().isLoading) {
                return
            }
            dispatch(LoadingStateChanged(true))
            scope.launch {
                var formattedServerAddress = serverAddress
                if (!formattedServerAddress.startsWith("http://") && !formattedServerAddress.startsWith(
                        "https://"
                    )
                ) {
                    formattedServerAddress = "https://$formattedServerAddress"
                }
                when (state().credentialType) {
                    WelcomeStore.CredentialType.CREDENTIALS -> authRepository.login(
                        formattedServerAddress,
                        username,
                        password
                    )

                    WelcomeStore.CredentialType.API_TOKEN -> authRepository.login(
                        formattedServerAddress,
                        password
                    )
                }.onError { error ->
                    val message = when (error) {
                        LoginError.InvalidCredentials -> Res.string.login_failed
                        LoginError.UnresolvedAddress,
                        LoginError.NetworkError -> Res.string.unreachable_server_address

                        LoginError.UnknownError -> Res.string.something_went_wrong
                    }
                    publish(WelcomeStore.Label.LoginFailed(message))
                }
                dispatch(LoadingStateChanged(false))
            }
        }
    }

    private object WelcomeReducerImpl :
        Reducer<WelcomeStore.State, Message> {
        override fun WelcomeStore.State.reduce(
            msg: Message
        ): WelcomeStore.State = when (msg) {
            is ServerAddressChanged -> copy(
                serverAddress = msg.serverAddress,
                isLoginButtonEnabled = isLoginButtonEnabled(
                    credentialType,
                    msg.serverAddress,
                    username,
                    password
                )
            )

            is PasswordChanged -> copy(
                password = msg.password,
                isLoginButtonEnabled = isLoginButtonEnabled(
                    credentialType,
                    serverAddress,
                    username,
                    msg.password
                )
            )

            is UsernameChanged -> copy(
                username = msg.username,
                isLoginButtonEnabled = isLoginButtonEnabled(
                    credentialType,
                    serverAddress,
                    msg.username,
                    password
                )
            )

            is LoadingStateChanged -> copy(isLoading = msg.isLoading)
            is Message.CredentialTypeChanged -> copy(
                credentialType = msg.credentialType,
                isLoginButtonEnabled = isLoginButtonEnabled(
                    msg.credentialType,
                    serverAddress,
                    username,
                    password
                )
            )
        }

        private fun isLoginButtonEnabled(
            credentialType: WelcomeStore.CredentialType,
            serverAddress: String,
            username: String,
            password: String
        ) = when (credentialType) {
            WelcomeStore.CredentialType.CREDENTIALS -> serverAddress.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            WelcomeStore.CredentialType.API_TOKEN -> serverAddress.isNotBlank() && password.isNotBlank()
        }
    }
}