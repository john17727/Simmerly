package dev.juanrincon.simmerly.recipes.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UnitDto(
    val id: String,
    val name: String,
    val pluralName: String,
    val description: String,
    val fraction: Boolean,
    val abbreviation: String,
    val pluralAbbreviation: String?,
    val useAbbreviation: Boolean,
//    val aliases: List<String>, // Assuming this is a list of strings TODO: Check if this is correct
    val createdAt: String,
    val updatedAt: String
)
