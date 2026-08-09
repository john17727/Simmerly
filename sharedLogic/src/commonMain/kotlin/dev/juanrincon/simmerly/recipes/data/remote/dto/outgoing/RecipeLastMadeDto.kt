package dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing

import kotlinx.serialization.Serializable

/** Body for `PATCH /api/recipes/{slug}/last-made`. [timestamp] is an ISO-8601 instant string. */
@Serializable
data class RecipeLastMadeDto(
    val timestamp: String
)
