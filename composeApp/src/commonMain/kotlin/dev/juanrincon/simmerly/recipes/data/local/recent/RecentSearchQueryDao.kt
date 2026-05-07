package dev.juanrincon.simmerly.recipes.data.local.recent

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchQueryDao {
    @Upsert
    suspend fun upsert(entity: RecentSearchQueryEntity)

    @Query("SELECT * FROM recent_search_queries ORDER BY searchedAt DESC LIMIT 10")
    fun observe(): Flow<List<RecentSearchQueryEntity>>
}
