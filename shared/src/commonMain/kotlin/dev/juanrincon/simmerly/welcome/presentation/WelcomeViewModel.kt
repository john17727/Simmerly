package dev.juanrincon.simmerly.welcome.presentation

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.welcome.presentation.model.CredentialType
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeIntent
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeSideEffect
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeState
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

class WelcomeViewModel(
    private val authRepository: AuthRepository,
) : OrbitContainerHost<WelcomeState, WelcomeState, WelcomeSideEffect>, ViewModel() {

    override val container: OrbitContainer<WelcomeState, WelcomeState, WelcomeSideEffect> =
        orbitContainer(initialState = WelcomeState())

    fun onEvent(event: WelcomeIntent) {
        when (event) {
            is WelcomeIntent.OnCredentialTypeChanged -> intent {
                reduce { state.copy(credentialType = event.credentialType) }
            }

            is WelcomeIntent.OnServerAddressChanged -> intent {
                reduce { state.copy(serverAddress = event.value) }
            }

            is WelcomeIntent.OnUsernameChanged -> intent {
                reduce { state.copy(username = event.value) }
            }

            is WelcomeIntent.OnPasswordChanged -> intent {
                reduce { state.copy(password = event.value) }
            }

            WelcomeIntent.OnLoginClicked -> intent {
                if (state.isLoading) {
                    return@intent
                }

                reduce { state.copy(isLoading = true) }

                val address = state.serverAddress
                val user = state.username
                val pass = state.password

                val formattedAddress = if (address.startsWith("http://") || address.startsWith("https://")) {
                    address
                } else {
                    "https://$address"
                }

                when (state.credentialType) {
                    CredentialType.CREDENTIALS -> authRepository.login(formattedAddress, user, pass)
                    CredentialType.API_TOKEN -> authRepository.login(formattedAddress, pass)
                }.onLeft { error ->
                    val message = when (error) {
                        LoginError.InvalidCredentials -> StringKey.LoginFailed
                        LoginError.UnresolvedAddress,
                        LoginError.NetworkError -> StringKey.UnreachableServerAddress
                        LoginError.UnknownError -> StringKey.SomethingWentWrong
                    }
                    postSideEffect(WelcomeSideEffect.LoginFailed(UiText.Resource(message)))
                }
                reduce { state.copy(isLoading = false) }
            }
        }
    }
}
