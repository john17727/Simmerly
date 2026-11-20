package dev.juanrincon.simmerly.core.data.local

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.juanrincon.simmerly.core.buildTestDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

class CacheMetadataDaoTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var dao: CacheMetadataDao

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        dao = db.cacheMetadataDao()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun getReturnsNullForUnknownKey() = runTest {
        assertThat(dao.get("missing")).isNull()
    }

    @Test
    fun upsertThenGetReturnsTheEntity() = runTest {
        val entity = CacheMetadataEntity("k", Instant.fromEpochSeconds(1_000))
        dao.upsert(entity)
        assertThat(dao.get("k")).isEqualTo(entity)
    }

    @Test
    fun upsertReplacesTheExistingTimestamp() = runTest {
        dao.upsert(CacheMetadataEntity("k", Instant.fromEpochSeconds(1_000)))
        dao.upsert(CacheMetadataEntity("k", Instant.fromEpochSeconds(2_000)))
        assertThat(dao.get("k")?.lastSyncedAt).isEqualTo(Instant.fromEpochSeconds(2_000))
    }

    @Test
    fun deleteRemovesOnlyThatKey() = runTest {
        dao.upsert(CacheMetadataEntity("a", Instant.fromEpochSeconds(1)))
        dao.upsert(CacheMetadataEntity("b", Instant.fromEpochSeconds(1)))
        dao.delete("a")
        assertThat(dao.get("a")).isNull()
        assertThat(dao.get("b")?.key).isEqualTo("b")
    }
}
