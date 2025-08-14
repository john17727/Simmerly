package dev.juanrincon.simmerly.auth.domain

import kotlinx.coroutines.flow.Flow

interface SessionStorage {

    suspend fun getToken(): String?

    suspend fun setToken(token: String)

    fun isAuthenticated(): Flow<AuthState>

    suspend fun clear()
}