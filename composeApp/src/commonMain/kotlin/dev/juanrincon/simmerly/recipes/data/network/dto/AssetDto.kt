package dev.juanrincon.simmerly.recipes.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssetDto(
    val name: String,
    val icon: String,
    val fileName: String
)
