package dev.juanrincon.simmerly.auth.domain

import app.tracktion.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    suspend fun login(serverAddress: String, username: String, password: String) : EmptyResult<LoginError>
    suspend fun login(serverAddress: String, apiKey: String): EmptyResult<LoginError>
    suspend fun logout()
}