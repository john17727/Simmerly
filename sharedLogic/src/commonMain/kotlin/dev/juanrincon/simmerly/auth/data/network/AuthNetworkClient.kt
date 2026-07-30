package dev.juanrincon.simmerly.auth.data.network

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import dev.juanrincon.simmerly.auth.data.network.dto.AuthTokenResponse
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandler
import dev.juanrincon.simmerly.core.data.remote.utils.dynamic_base_url_ktor_plugin.BaseUrlOverride
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters

class AuthNetworkClient(private val httpClient: HttpClient) {
    suspend fun logIn(
        baseUrl: String,
        username: String,
        password: String
    ): Either<DataError.NetworkError<Unit>, String> =
        arrowNetworkHandler<Unit, AuthTokenResponse> {
            httpClient.submitForm(
                url = "/api/auth/token",
                formParameters = Parameters.build {
                    set("username", username)
                    set("password", password)
                }
            ) {
                attributes.put(BaseUrlOverride, baseUrl)
            }
        }.map { it.token }
}