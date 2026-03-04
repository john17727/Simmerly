package dev.juanrincon.simmerly.core.data.remote

import dev.juanrincon.simmerly.auth.data.network.dto.AuthTokenResponse
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.data.remote.utils.dynamic_base_url_ktor_plugin.DynamicBaseUrl
import dev.juanrincon.simmerly.core.data.remote.utils.dynamic_base_url_ktor_plugin.supplier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createAuthenticatedHttpClient(
    engine: HttpClientEngine,
    sessionDatastore: SessionDataStore
) = HttpClient(engine) {
    install(HttpTimeout) {
        requestTimeoutMillis = 10000
        connectTimeoutMillis = 5000
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {
        json(
            json = Json {
                ignoreUnknownKeys = true
            }
        )
    }
    defaultRequest {
        contentType(ContentType.Application.Json)
        url("https://fallback.com") // Will be replaced
    }
    install(Auth) {
        bearer {
            loadTokens {
                val token = sessionDatastore.getToken().orEmpty()
                if (token.isBlank()) {
                    null
                } else {
                    // Single-token model → use the same token for access & refresh
                    BearerTokens(token, token)
                }
            }
            refreshTokens {
                val serverAddress = sessionDatastore.getServerAddress() ?: return@refreshTokens null
                val oldRefresh = oldTokens?.refreshToken ?: return@refreshTokens null

                // Option A: explicit status check
                val response = try {
                    client.get("$serverAddress/api/auth/refresh") {
                        markAsRefreshTokenRequest()
                        headers {
                            append("Authorization", "Bearer $oldRefresh")
                        }
                    }
                } catch (e: Exception) {
                    // Network/serialization error -> treat as refresh failure
                    sessionDatastore.clear()
                    return@refreshTokens null
                }

                if (!response.status.isSuccess()) {
                    // Refresh denied -> logout
                    sessionDatastore.clear()
                    return@refreshTokens null
                }

                val tokenResult: AuthTokenResponse = try {
                    response.body()
                } catch (e: Exception) {
                    sessionDatastore.clear()
                    return@refreshTokens null
                }

                val accessToken = tokenResult.token // or tokenResult.accessToken

                if (accessToken.isBlank()) {
                    sessionDatastore.clear()
                    return@refreshTokens null
                }

                sessionDatastore.setToken(accessToken)
                BearerTokens(accessToken, accessToken)
            }
        }
    }
    install(DynamicBaseUrl) {
        supplier {
            rewriteAbsolute = true
            loadBaseUrl {
                sessionDatastore.getServerAddress()
            }
        }
    }
}