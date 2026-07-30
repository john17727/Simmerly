package dev.juanrincon.simmerly.auth.domain

import arrow.core.Either
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    suspend fun login(
        serverAddress: String,
        username: String,
        password: String
    ): Either<LoginError, Unit>

    suspend fun login(serverAddress: String, apiKey: String): Either<LoginError, Unit>
    suspend fun logout()
}