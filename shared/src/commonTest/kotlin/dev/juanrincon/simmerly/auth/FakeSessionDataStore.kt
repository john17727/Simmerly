package dev.juanrincon.simmerly.auth

import dev.juanrincon.simmerly.auth.domain.AuthState
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionDataStore : SessionDataStore {
    private val serverAddressFlow = MutableStateFlow<String?>(null)
    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Loading)

    var serverAddress: String? = null
    var token: String? = null

    override suspend fun setServerAddress(address: String) {
        serverAddress = address
        serverAddressFlow.value = address
    }

    override suspend fun getServerAddress(): String? = serverAddress

    override fun observeServerAddress(): Flow<String?> = serverAddressFlow

    override suspend fun getToken(): String? = token

    override suspend fun setToken(token: String) {
        this.token = token
        authStateFlow.value = AuthState.Authenticated
    }

    override fun isAuthenticated(): Flow<AuthState> = authStateFlow

    override suspend fun clear() {
        serverAddress = null
        token = null
        serverAddressFlow.value = null
        authStateFlow.value = AuthState.Unauthenticated
    }

    fun emitAuthState(state: AuthState) {
        authStateFlow.value = state
    }
}
