package dev.juanrincon.simmerly.auth.data.network

import app.tracktion.core.domain.util.DataError
import app.tracktion.core.domain.util.Result
import app.tracktion.core.domain.util.mapData
import dev.juanrincon.simmerly.auth.data.network.dto.AuthTokenResponse
import dev.juanrincon.simmerly.core.data.network.utils.BaseUrlOverride
import dev.juanrincon.simmerly.core.data.network.networkHandler
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters

class AuthNetworkClient(private val httpClient: HttpClient) {
    suspend fun logIn(
        baseUrl: String,
        username: String,
        password: String
    ): Result<String, DataError.NetworkError<Unit>> =
        networkHandler<AuthTokenResponse, Unit> {
            httpClient.submitForm(
                url = "/api/auth/token",
                formParameters = Parameters.build {
                    set("username", username)
                    set("password", password)
                }
            ) {
                attributes.put(BaseUrlOverride, baseUrl)
            }
        }.mapData { it.token }
}