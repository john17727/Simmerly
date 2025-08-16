package dev.juanrincon.simmerly.recipes.data.network.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val groupId: String,
    val name: String,
    val slug: String
)
