package dev.juanrincon.simmerly.core.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Upsert
import kotlin.time.Instant

/** When a cached dataset was last successfully synced from the server, keyed by [CacheKeys]. */
@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey
    val key: String,
    val lastSyncedAt: Instant,
)

@Dao
interface CacheMetadataDao {
    @Upsert
    suspend fun upsert(entity: CacheMetadataEntity)

    @Query("SELECT * FROM cache_metadata WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): CacheMetadataEntity?

    @Query("DELETE FROM cache_metadata WHERE `key` = :key")
    suspend fun delete(key: String)
}
