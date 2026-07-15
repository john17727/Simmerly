package dev.juanrincon.simmerly.welcome.presentation

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.welcome.presentation.model.CredentialType
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeIntent
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeSideEffect
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeState
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import simmerly.shared.generated.resources.Res
import simmerly.shared.generated.resources.login_failed
import simmerly.shared.generated.resources.something_went_wrong
import simmerly.shared.generated.resources.unreachable_server_address

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

            WelcomeIntent.OnLoginClicked -> intent {
                if (state.isLoading) {
                    return@intent
                }

                reduce { state.copy(isLoading = true) }

                val address = state.serverAddress.text.toString()
                val user = state.username.text.toString()
                val pass = state.password.text.toString()

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
                        LoginError.InvalidCredentials -> Res.string.login_failed
                        LoginError.UnresolvedAddress,
                        LoginError.NetworkError -> Res.string.unreachable_server_address
                        LoginError.UnknownError -> Res.string.something_went_wrong
                    }
                    postSideEffect(WelcomeSideEffect.LoginFailed(message))
                }
                reduce { state.copy(isLoading = false) }
            }
        }
    }
}
