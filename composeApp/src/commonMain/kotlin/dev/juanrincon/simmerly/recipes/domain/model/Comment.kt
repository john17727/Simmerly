package dev.juanrincon.simmerly.recipes.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Comment(
    val text: String,
    val id: String,
    val createdAt: Instant,
    val updatedAt: Instant,
//    val user: User //TODO: Add user
)
