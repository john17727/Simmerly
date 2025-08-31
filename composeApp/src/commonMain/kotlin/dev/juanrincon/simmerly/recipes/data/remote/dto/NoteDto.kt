package dev.juanrincon.simmerly.recipes.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val title: String,
    val text: String
)
