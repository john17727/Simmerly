package dev.juanrincon.simmerly.recipes.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Unit(
    val id: String,
    val name: String,
    val pluralName: String,
    val description: String,
    val fraction: Boolean,
    val abbreviation: String,
    val pluralAbbreviation: String?,
    val useAbbreviation: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
