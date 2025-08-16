package dev.juanrincon.simmerly.auth.data

import app.tracktion.core.domain.util.DataError
import app.tracktion.core.domain.util.EmptyResult
import app.tracktion.core.domain.util.asEmptyDataResult
import app.tracktion.core.domain.util.mapError
import app.tracktion.core.domain.util.onSuccess
import dev.juanrincon.simmerly.auth.data.network.AuthNetworkClient
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.AuthState
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import kotlinx.coroutines.flow.Flow

class DefaultAuthRepository(
    val sessionDatastore: SessionDataStore,
    val networkClient: AuthNetworkClient
) : AuthRepository {
    override fun observeAuthState(): Flow<AuthState> = sessionDatastore.isAuthenticated()

    override suspend fun login(
        serverAddress: String,
        username: String,
        password: String
    ): EmptyResult<LoginError> =
        networkClient.logIn(serverAddress, username, password).onSuccess {
            sessionDatastore.setServerAddress(serverAddress)
            sessionDatastore.setToken(it)
        }.mapError { error ->
            when (error) {
                is DataError.NetworkError.BadRequest<*>,
                DataError.NetworkError.Unauthorized -> LoginError.InvalidCredentials

                DataError.NetworkError.ServerError,
                DataError.NetworkError.NoInternet -> LoginError.NetworkError

                DataError.NetworkError.RequestTimeout,
                DataError.NetworkError.UnresolvedAddress -> LoginError.UnresolvedAddress

                else -> LoginError.UnknownError
            }
        }.asEmptyDataResult()

    override suspend fun login(apiKey: String) {
        TODO("Not yet implemented")
    }

    override suspend fun logout() {
        TODO("Not yet implemented")
    }
}