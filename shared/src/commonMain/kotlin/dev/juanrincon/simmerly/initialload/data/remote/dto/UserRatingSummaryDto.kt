package dev.juanrincon.simmerly.initialload.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserRatingSummaryDto(
    val recipeId: String,
    val rating: Double?,
    val isFavorite: Boolean,
)

@Serializable
data class UserRatingsDto(
    val ratings: List<UserRatingSummaryDto>
)
