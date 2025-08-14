package dev.juanrincon.simmerly.auth.data

import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DefaultAuthRepository: AuthRepository {
    override fun observeAuthState(): Flow<AuthState> = flowOf(AuthState.Unauthenticated)

    override suspend fun login(username: String, password: String) {
        TODO("Not yet implemented")
    }

    override suspend fun login(apiKey: String) {
        TODO("Not yet implemented")
    }

    override suspend fun logout() {
        TODO("Not yet implemented")
    }
}