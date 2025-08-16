package dev.juanrincon.simmerly.core.domain.network

interface TokenProvider {
    suspend fun getToken(): String?
    suspend fun setToken(token: String)
}