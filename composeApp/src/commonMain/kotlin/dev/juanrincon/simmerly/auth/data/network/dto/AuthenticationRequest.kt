package dev.juanrincon.simmerly.auth.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class AuthenticationRequest(
    val username: String,
    val password: String,
    val rememberMe: Boolean = false
)
