package dev.juanrincon.simmerly.recipes.data.remote.dto

import kotlinx.serialization.Serializable

/** Response of `POST /api/recipes/timeline/events`. Decoded only to confirm the write succeeded —
 * the app has no local timeline table or UI to persist it into yet (the `ViewTimeline` icon on the
 * recipe details bottom bar is still a TODO). */
@Serializable
data class RecipeTimelineEventOutDto(
    val id: String,
    val recipeId: String,
    val userId: String,
    val subject: String,
    val eventType: String,
    val eventMessage: String? = null,
    val image: String? = null,
    val timestamp: String,
    val groupId: String,
    val householdId: String,
    val createdAt: String,
    val updatedAt: String
)
