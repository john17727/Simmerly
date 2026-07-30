package dev.juanrincon.simmerly.auth.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthTokenResponse(
    @SerialName("access_token")
    val token: String,
    @SerialName("token_type")
    val tokenType: String,
)