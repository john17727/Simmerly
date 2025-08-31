package dev.juanrincon.simmerly.recipes.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Food(
    val id: String,
    val name: String,
    val pluralName: String,
    val description: String,
    val labelId: String,
    val label: Label,
    val createdAt: Instant,
    val updatedAt: Instant
)
