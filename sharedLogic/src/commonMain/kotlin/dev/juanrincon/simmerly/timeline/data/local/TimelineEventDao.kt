package dev.juanrincon.simmerly.timeline.data.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineEventDao {

    @Upsert
    suspend fun upsertAll(events: List<TimelineEventEntity>)

    @Transaction
    @Query("SELECT * FROM timeline_events WHERE recipe_id = :recipeId ORDER BY timestamp DESC")
    fun observe(recipeId: String): Flow<List<TimelineEventWithUser>>

    @Query("SELECT COUNT(*) FROM timeline_events WHERE recipe_id = :recipeId")
    suspend fun count(recipeId: String): Int

    @Query("DELETE FROM timeline_events WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)

    /** Drops cached rows of [recipeId] that the server no longer returned in a refreshed first page. */
    @Query("DELETE FROM timeline_events WHERE recipe_id = :recipeId AND id NOT IN (:keepIds)")
    suspend fun deleteMissing(recipeId: String, keepIds: List<String>)

    @Query("DELETE FROM timeline_events WHERE id = :id")
    suspend fun deleteById(id: String)
}
