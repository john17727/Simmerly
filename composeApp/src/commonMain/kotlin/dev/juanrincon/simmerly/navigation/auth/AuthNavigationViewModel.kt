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
): ViewModel() {

    val isAuthenticated: StateFlow<AuthDestinations> =
        repository.observeAuthState().map { authState ->
        when (authState) {
            is AuthState.Authenticated -> AuthDestinations.App
            AuthState.Loading -> AuthDestinations.Splash
            AuthState.Unauthenticated -> AuthDestinations.Login
        }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, AuthDestinations.Splash)

}