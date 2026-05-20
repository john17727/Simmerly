package dev.juanrincon.simmerly.auth.domain

sealed interface AuthState {
    data object Authenticated : AuthState
    data object Unauthenticated : AuthState
    data object Loading : AuthState // Optional: for initial check
}