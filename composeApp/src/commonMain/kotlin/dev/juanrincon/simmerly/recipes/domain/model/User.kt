package dev.juanrincon.simmerly.recipes.domain.model

data class User(
    val id: String,
    val username: String,
    val admin: Boolean,
    val fullName: String
)
