package dev.juanrincon.simmerly.core.data.network

import dev.juanrincon.simmerly.auth.domain.SessionStorage
import dev.juanrincon.simmerly.core.domain.network.TokenProvider

class DataStoreTokenProvider(
    private val sessionStorage: SessionStorage,
): TokenProvider {
    override suspend fun getToken(): String? = sessionStorage.getToken()

    override suspend fun setToken(token: String) = sessionStorage.setToken(token)
}