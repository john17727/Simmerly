package dev.juanrincon.simmerly.recipes.data.local.recent

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "recent_search_queries")
data class RecentSearchQueryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long
)
