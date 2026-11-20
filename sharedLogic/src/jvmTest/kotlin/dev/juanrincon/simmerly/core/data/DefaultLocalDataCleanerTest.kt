package dev.juanrincon.simmerly.core.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.juanrincon.simmerly.core.aRecipeEntity
import dev.juanrincon.simmerly.core.aUserEntity
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.core.domain.ResettableCache
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DefaultLocalDataCleanerTest {

    private lateinit var db: SimmerlyDatabase

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun clearAllEmptiesTheDatabase() = runTest {
        db.recipeDao().upsert(aRecipeEntity(id = "r1"))
        db.userDao().upsert(aUserEntity())
        val cleaner = DefaultLocalDataCleaner(db, emptyList())

        cleaner.clearAll()

        assertThat(db.recipeDao().existsById("r1")).isEqualTo(0)
        assertThat(db.userDao().getById("user-1")).isNull()
    }

    @Test
    fun clearAllResetsEveryRegisteredCache() = runTest {
        val caches = List(2) { CountingCache() }
        val cleaner = DefaultLocalDataCleaner(db, caches)

        cleaner.clearAll()

        assertThat(caches.map { it.resetCount }).isEqualTo(listOf(1, 1))
    }

    private class CountingCache : ResettableCache {
        var resetCount = 0
        override fun reset() {
            resetCount++
        }
    }
}
