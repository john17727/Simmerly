package dev.juanrincon.simmerly.auth.data.network

import app.tracktion.core.domain.util.DataError
import app.tracktion.core.domain.util.Result
import app.tracktion.core.domain.util.mapData
import dev.juanrincon.simmerly.auth.data.network.dto.AuthTokenResponse
import dev.juanrincon.simmerly.auth.data.network.dto.AuthenticationRequest
import dev.juanrincon.simmerly.core.data.network.networkHandler
import dev.juanrincon.simmerly.core.data.network.withApiUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthNetworkClient(private val httpClient: HttpClient) {
    suspend fun logIn(
        baseUrl: String,
        username: String,
        password: String
    ): Result<String, DataError.NetworkError<Unit>> =
        networkHandler<AuthTokenResponse, Unit> {
            httpClient.post {
                withApiUrl(baseUrl, "/api/auth/token")
                setBody(AuthenticationRequest(username, password))
            }
        }.mapData { it.token }
}