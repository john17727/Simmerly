package dev.juanrincon.simmerly.auth.domain

import kotlinx.coroutines.flow.Flow

interface SessionDataStore {

    suspend fun setServerAddress(address: String)

    suspend fun getServerAddress(): String?

    fun observeServerAddress(): Flow<String?>

    suspend fun getToken(): String?

    suspend fun setToken(token: String)

    /** The logged-in user's Mealie UUID, if it has been resolved yet — see
     * [dev.juanrincon.simmerly.initialload.domain.UserRepository.currentUserId] for a fallback
     * that fetches and persists it when this is null (e.g. a session that predates this field). */
    suspend fun getUserId(): String?

    suspend fun setUserId(id: String)

    fun isAuthenticated(): Flow<AuthState>

    suspend fun clear()
}