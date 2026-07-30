package dev.juanrincon.simmerly.navigation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.AuthState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AuthNavigationViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    val isAuthenticated: StateFlow<AuthRoute> =
        repository.observeAuthState().map { authState ->
            when (authState) {
                is AuthState.Authenticated -> AuthRoute.InitialLoad
                AuthState.Loading -> AuthRoute.Splash
                AuthState.Unauthenticated -> AuthRoute.Login
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, AuthRoute.Splash)

}