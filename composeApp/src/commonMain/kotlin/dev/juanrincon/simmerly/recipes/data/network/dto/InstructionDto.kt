package dev.juanrincon.simmerly.recipes.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class InstructionDto(
    val id: String,
    val title: String,
    val summary: String,
    val text: String,
    val ingredientReferences: List<ReferenceDto>
)
