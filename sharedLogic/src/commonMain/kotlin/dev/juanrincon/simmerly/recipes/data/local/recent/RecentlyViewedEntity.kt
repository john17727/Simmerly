package dev.juanrincon.simmerly.recipes.data.local.recent

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "recently_viewed")
data class RecentlyViewedEntity(
    @PrimaryKey val recipeId: String,
    val viewedAt: Long
)
