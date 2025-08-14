package dev.juanrincon.simmerly.auth.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    suspend fun login(username: String, password: String)
    suspend fun login(apiKey: String)
    suspend fun logout()
}