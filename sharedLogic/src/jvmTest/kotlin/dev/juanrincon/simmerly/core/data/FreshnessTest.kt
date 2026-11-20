package dev.juanrincon.simmerly.core.data

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class FreshnessTest {

    private class FakeClock(var now: Instant) : Clock {
        override fun now(): Instant = now
    }

    private val ttl = 10.minutes
    private val start = Instant.fromEpochSeconds(1_000_000)
    private lateinit var db: SimmerlyDatabase
    private lateinit var clock: FakeClock
    private lateinit var freshness: Freshness

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        clock = FakeClock(start)
        freshness = Freshness(db.cacheMetadataDao(), clock)
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun neverSyncedKeyIsStale() = runTest {
        assertThat(freshness.isStale("k", ttl)).isTrue()
    }

    @Test
    fun justSyncedKeyIsFresh() = runTest {
        freshness.markSynced("k")
        assertThat(freshness.isStale("k", ttl)).isFalse()
    }

    @Test
    fun keyIsFreshUntilTtlElapses() = runTest {
        freshness.markSynced("k")
        clock.now = start + ttl - 1.minutes
        assertThat(freshness.isStale("k", ttl)).isFalse()
    }

    @Test
    fun keyIsStaleOnceTtlElapses() = runTest {
        freshness.markSynced("k")
        clock.now = start + ttl
        assertThat(freshness.isStale("k", ttl)).isTrue()
    }

    @Test
    fun syncTimeInTheFutureIsStale() = runTest {
        freshness.markSynced("k")
        clock.now = start - 5.minutes
        assertThat(freshness.isStale("k", ttl)).isTrue()
    }

    @Test
    fun keysAreIndependent() = runTest {
        freshness.markSynced("a")
        assertThat(freshness.isStale("b", ttl)).isTrue()
    }

    @Test
    fun invalidateMakesKeyStale() = runTest {
        freshness.markSynced("k")
        freshness.invalidate("k")
        assertThat(freshness.isStale("k", ttl)).isTrue()
    }

    @Test
    fun logoutStyleClearWipesFreshnessToo() = runTest {
        freshness.markSynced("k")
        DefaultLocalDataCleaner(db, emptyList()).clearAll()
        assertThat(freshness.isStale("k", ttl)).isTrue()
    }
}
