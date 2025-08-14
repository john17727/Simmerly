package dev.juanrincon.simmerly.auth.data

import app.tracktion.core.domain.util.fold
import dev.juanrincon.simmerly.auth.data.network.AuthNetworkClient
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.AuthState
import dev.juanrincon.simmerly.auth.domain.SessionStorage
import kotlinx.coroutines.flow.Flow

class DefaultAuthRepository(
    val sessionStorage: SessionStorage,
    val networkClient: AuthNetworkClient
) : AuthRepository {
    override fun observeAuthState(): Flow<AuthState> = sessionStorage.isAuthenticated()

    override suspend fun login(serverAddress: String, username: String, password: String) {
        networkClient.logIn(serverAddress, username, password).fold(
            onSuccess = {
                sessionStorage.setServerAddress(serverAddress)
                sessionStorage.setToken(it)
            },
            onFailure = {}
        )
    }

    override suspend fun login(apiKey: String) {
        TODO("Not yet implemented")
    }

    override suspend fun logout() {
        TODO("Not yet implemented")
    }
}