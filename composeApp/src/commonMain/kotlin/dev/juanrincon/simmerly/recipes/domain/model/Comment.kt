package dev.juanrincon.simmerly.recipes.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Comment(
    val recipeId: String,
    val text: String,
    val id: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val userId: String,
    val user: User
)
