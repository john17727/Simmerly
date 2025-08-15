package dev.juanrincon.simmerly.auth.domain

import app.tracktion.core.domain.util.EmptyResult
import app.tracktion.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    suspend fun login(serverAddress: String, username: String, password: String) : EmptyResult<LoginError>
    suspend fun login(apiKey: String)
    suspend fun logout()
}