package dev.juanrincon.simmerly.core.data

import dev.juanrincon.simmerly.core.data.local.CacheMetadataDao
import dev.juanrincon.simmerly.core.data.local.CacheMetadataEntity
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

/** Identifies a cached dataset in `cache_metadata`. */
object CacheKeys {
    const val RECIPE_LIST = "recipe_list"
    fun referenceData(name: String) = "reference_data:$name"
    const val SHOPPING_LISTS = "shopping_lists"
    fun recipeDetail(id: String) = "recipe_detail:$id"
}

/** How long a synced dataset counts as fresh. Tuned here, in one place. */
object CacheTtl {
    val RECIPE_LIST = 15.minutes
    val REFERENCE_DATA = 24.hours
    val RECIPE_DETAIL = 5.minutes
    val SHOPPING_LISTS = 1.minutes
}

/**
 * The single freshness policy: a dataset is stale when it was never synced or its last sync is
 * older than its TTL. Pull-to-refresh and other explicit refreshes skip [isStale] and go straight
 * to the network, then call [markSynced].
 */
@OptIn(ExperimentalTime::class)
class Freshness(
    private val dao: CacheMetadataDao,
    private val clock: Clock = Clock.System,
) {
    suspend fun isStale(key: String, ttl: Duration): Boolean {
        val syncedAt = dao.get(key)?.lastSyncedAt ?: return true
        // A clock moved backwards leaves a "future" sync time; treat that as stale, not fresh.
        val age = clock.now() - syncedAt
        return age < Duration.ZERO || age >= ttl
    }

    suspend fun markSynced(key: String) {
        dao.upsert(CacheMetadataEntity(key, clock.now()))
    }

    suspend fun invalidate(key: String) {
        dao.delete(key)
    }
}
