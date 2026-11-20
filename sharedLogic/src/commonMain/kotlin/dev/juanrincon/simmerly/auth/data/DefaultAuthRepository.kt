package dev.juanrincon.simmerly.auth.data

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import dev.juanrincon.simmerly.auth.data.network.AuthNetworkClient
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.AuthState
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.domain.LocalDataCleaner
import kotlinx.coroutines.flow.Flow

class DefaultAuthRepository(
    val sessionDatastore: SessionDataStore,
    val networkClient: AuthNetworkClient,
    private val localDataCleaner: LocalDataCleaner,
) : AuthRepository {
    override fun observeAuthState(): Flow<AuthState> = sessionDatastore.isAuthenticated()

    override suspend fun login(
        serverAddress: String,
        username: String,
        password: String
    ): Either<LoginError, Unit> = either {
        // TODO: Validate token by getting user data, potentially get auxiliary data from the server
        val token = networkClient.logIn(serverAddress, username, password)
            .mapLeft { error ->
                when (error) {
                    is DataError.NetworkError.BadRequest<*>,
                    DataError.NetworkError.Unauthorized -> LoginError.InvalidCredentials

                    DataError.NetworkError.ServerError,
                    DataError.NetworkError.NoInternet -> LoginError.NetworkError

                    DataError.NetworkError.RequestTimeout,
                    DataError.NetworkError.UnresolvedAddress -> LoginError.UnresolvedAddress

                    else -> LoginError.UnknownError
                }
            }.bind()
        sessionDatastore.setServerAddress(serverAddress)
        sessionDatastore.setToken(token)
    }

    override suspend fun login(serverAddress: String, apiKey: String): Either<LoginError, Unit> {
        // TODO: Validate API key by getting user data, potentially get auxiliary data from the server
        sessionDatastore.setServerAddress(serverAddress)
        sessionDatastore.setToken(apiKey)
        return Unit.right()
    }

    override suspend fun logout() {
        // Session first, so the UI leaves the signed-in screens before their data disappears.
        sessionDatastore.clear()
        localDataCleaner.clearAll()
    }
}