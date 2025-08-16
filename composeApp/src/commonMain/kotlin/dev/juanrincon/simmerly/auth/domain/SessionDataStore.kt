package dev.juanrincon.simmerly.auth.domain

import kotlinx.coroutines.flow.Flow

interface SessionDataStore {

    suspend fun setServerAddress(address: String)

    suspend fun getServerAddress(): String?

    fun observeServerAddress(): Flow<String?>

    suspend fun getToken(): String?

    suspend fun setToken(token: String)

    fun isAuthenticated(): Flow<AuthState>

    suspend fun clear()
}