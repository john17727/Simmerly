package dev.juanrincon.simmerly.core.data

import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.core.domain.LocalDataCleaner
import dev.juanrincon.simmerly.core.domain.ResettableCache

class DefaultLocalDataCleaner(
    private val database: SimmerlyDatabase,
    private val caches: List<ResettableCache>,
) : LocalDataCleaner {
    override suspend fun clearAll() {
        caches.forEach { it.reset() }
        database.clearAllTables()
    }
}
