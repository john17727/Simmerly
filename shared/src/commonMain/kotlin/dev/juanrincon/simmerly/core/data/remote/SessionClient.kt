package dev.juanrincon.simmerly.core.data.remote

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import dev.juanrincon.simmerly.auth.data.network.dto.AuthError
import dev.juanrincon.simmerly.auth.data.network.dto.AuthTokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers

internal class SessionClient(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun refreshToken(currentToken: String): Either<DataError.NetworkError<AuthError>, String> =
        arrowNetworkHandler<AuthError, AuthTokenResponse>(
            call = {
                httpClient.get {
                    withApiUrl(baseUrl, "/api/auth/refresh")
                    headers {
                        append("Authorization", currentToken)
                    }
                }
            }
        ).map { it.token }
}