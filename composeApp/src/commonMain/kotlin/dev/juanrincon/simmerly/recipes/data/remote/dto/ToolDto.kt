package dev.juanrincon.simmerly.recipes.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ToolDto(
    val id: String,
    val groupId: String,
    val name: String,
    val slug: String
)
