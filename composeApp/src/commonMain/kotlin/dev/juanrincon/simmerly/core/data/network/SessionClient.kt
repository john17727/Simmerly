package dev.juanrincon.simmerly.core.data.network

import app.tracktion.core.domain.util.DataError
import app.tracktion.core.domain.util.Result
import app.tracktion.core.domain.util.mapData
import dev.juanrincon.simmerly.auth.data.network.dto.AuthError
import dev.juanrincon.simmerly.auth.data.network.dto.AuthTokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post

internal class SessionClient(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun refreshToken(currentToken: String): Result<String, DataError.NetworkError<AuthError>> =
        networkHandler<AuthTokenResponse, AuthError>(
            call = {
                httpClient.get {
                    withApiUrl(baseUrl, "/api/auth/refresh")
                    headers {
                        append("Authorization", currentToken)
                    }
                }
            }
        ).mapData { it.token }
}