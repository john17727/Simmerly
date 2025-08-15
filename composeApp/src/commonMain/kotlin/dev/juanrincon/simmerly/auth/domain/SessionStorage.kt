package dev.juanrincon.simmerly.auth.domain

import kotlinx.coroutines.flow.Flow

interface SessionStorage {

    suspend fun setServerAddress(address: String)

    suspend fun getServerAddress(): String?

    suspend fun getToken(): String?

    suspend fun setToken(token: String)

    fun isAuthenticated(): Flow<AuthState>

    suspend fun clear()
}