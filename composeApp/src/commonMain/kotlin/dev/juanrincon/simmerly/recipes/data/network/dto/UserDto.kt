package dev.juanrincon.simmerly.recipes.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val admin: Boolean,
    val fullName: String
)
