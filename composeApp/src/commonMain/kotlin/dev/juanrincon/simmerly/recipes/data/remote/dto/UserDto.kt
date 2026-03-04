package dev.juanrincon.simmerly.recipes.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val admin: Boolean,
    val fullName: String
)
