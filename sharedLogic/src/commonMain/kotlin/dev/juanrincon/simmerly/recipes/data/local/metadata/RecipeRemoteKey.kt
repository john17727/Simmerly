package dev.juanrincon.simmerly.recipes.data.local.metadata

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "recipe_remote_keys")
data class RecipeRemoteKey(
    @PrimaryKey
    val id: String,
    val nextKey: String?,
    val createdAt: Instant
)
