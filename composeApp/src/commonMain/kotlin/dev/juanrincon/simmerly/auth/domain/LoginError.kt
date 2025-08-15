package dev.juanrincon.simmerly.auth.domain

import app.tracktion.core.domain.util.Error

sealed interface LoginError: Error {
    data object InvalidCredentials : LoginError
    data object NetworkError : LoginError
    data object UnknownError : LoginError
    data object UnresolvedAddress : LoginError
}