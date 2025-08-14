package dev.juanrincon.simmerly.auth.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthError(
    val detail: String,
)
