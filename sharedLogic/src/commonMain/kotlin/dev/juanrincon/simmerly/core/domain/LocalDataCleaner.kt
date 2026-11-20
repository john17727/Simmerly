package dev.juanrincon.simmerly.core.domain

/** Wipes everything cached on this device for the signed-in account. */
interface LocalDataCleaner {
    suspend fun clearAll()
}

/**
 * An in-memory cache that outlives any screen (e.g. a repository-scoped paging cache). Registered
 * with Koin so [LocalDataCleaner] can drop it together with the database.
 */
interface ResettableCache {
    fun reset()
}
