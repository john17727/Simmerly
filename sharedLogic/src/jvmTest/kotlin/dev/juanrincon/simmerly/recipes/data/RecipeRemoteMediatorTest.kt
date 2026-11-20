package dev.juanrincon.simmerly.recipes.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator.InitializeAction
import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.CacheKeys
import dev.juanrincon.simmerly.core.data.CacheTtl
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalPagingApi::class)
class RecipeRemoteMediatorTest {

    private class FakeClock(var now: Instant) : Clock {
        override fun now(): Instant = now
    }

    private val start = Instant.fromEpochSeconds(1_000_000)
    private lateinit var db: SimmerlyDatabase
    private lateinit var clock: FakeClock
    private lateinit var freshness: Freshness
    private lateinit var mediator: RecipeRemoteMediator

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        clock = FakeClock(start)
        freshness = Freshness(db.cacheMetadataDao(), clock)
        val client = RecipeNetworkClient(HttpClient(MockEngine { error("no network expected") }))
        mediator = RecipeRemoteMediator(RecipeListSyncer(client, db, freshness), freshness)
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun neverSyncedListLaunchesAnInitialRefresh() = runTest {
        assertThat(mediator.initialize()).isEqualTo(InitializeAction.LAUNCH_INITIAL_REFRESH)
    }

    @Test
    fun freshListSkipsTheInitialRefresh() = runTest {
        freshness.markSynced(CacheKeys.RECIPE_LIST)
        assertThat(mediator.initialize()).isEqualTo(InitializeAction.SKIP_INITIAL_REFRESH)
    }

    @Test
    fun staleListLaunchesAnInitialRefresh() = runTest {
        freshness.markSynced(CacheKeys.RECIPE_LIST)
        clock.now = start + CacheTtl.RECIPE_LIST
        assertThat(mediator.initialize()).isEqualTo(InitializeAction.LAUNCH_INITIAL_REFRESH)
    }
}
