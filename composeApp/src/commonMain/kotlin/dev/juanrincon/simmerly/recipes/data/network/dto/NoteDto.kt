package dev.juanrincon.simmerly.recipes.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val title: String,
    val text: String
)
