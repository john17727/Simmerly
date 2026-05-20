package dev.juanrincon.simmerly.auth

import arrow.core.Either
import arrow.core.right
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.AuthState
import dev.juanrincon.simmerly.auth.domain.LoginError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {

    var loginResult: Either<LoginError, Unit> = Unit.right()
    var lastCredentialsLoginCall: Triple<String, String, String>? = null
    var lastApiTokenLoginCall: Pair<String, String>? = null
    var loginCallCount: Int = 0
    var logoutCallCount: Int = 0

    var shouldDelayLogin: Boolean = false
    private val loginGate = Channel<Unit>(capacity = 1)

    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Unauthenticated)

    override fun observeAuthState(): Flow<AuthState> = authStateFlow

    override suspend fun login(
        serverAddress: String,
        username: String,
        password: String
    ): Either<LoginError, Unit> {
        loginCallCount++
        lastCredentialsLoginCall = Triple(serverAddress, username, password)
        if (shouldDelayLogin) loginGate.receive()
        return loginResult
    }

    override suspend fun login(
        serverAddress: String,
        apiKey: String
    ): Either<LoginError, Unit> {
        loginCallCount++
        lastApiTokenLoginCall = Pair(serverAddress, apiKey)
        if (shouldDelayLogin) loginGate.receive()
        return loginResult
    }

    override suspend fun logout() {
        logoutCallCount++
    }

    fun releaseLogin() {
        loginGate.trySend(Unit)
    }

    fun emitAuthState(state: AuthState) {
        authStateFlow.value = state
    }
}
