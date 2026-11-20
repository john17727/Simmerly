package dev.juanrincon.simmerly.recipes.data

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.auth.FakeSessionDataStore
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.CacheTtl
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.initialload.FakeUserRepository
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClientTest
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant

class SimmerlyRecipeRepositoryDetailsTest {

    private class FakeClock(var now: Instant) : Clock {
        override fun now(): Instant = now
    }

    private val start = Instant.fromEpochSeconds(1_000_000)
    private lateinit var db: SimmerlyDatabase
    private lateinit var clock: FakeClock
    private lateinit var repository: SimmerlyRecipeRepository
    private var detailRequests = 0

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        clock = FakeClock(start)
        val engine = MockEngine { request ->
            if (request.url.encodedPath.startsWith("/api/recipes/")) detailRequests++
            respond(
                content = RecipeNetworkClientTest.RECIPE_DETAIL_JSON,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = RecipeNetworkClient(HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })
        repository = SimmerlyRecipeRepository(
            networkClient = client,
            database = db,
            sessionDataStore = FakeSessionDataStore(),
            userRepository = FakeUserRepository(),
            freshness = Freshness(db.cacheMetadataDao(), clock),
        )
    }

    @AfterTest
    fun tearDown() {
        repository.reset()
        db.close()
    }

    /** Collects until the first loaded emission and returns every result seen before it. */
    private suspend fun results(id: String = "recipe-1"): List<LoadingResult<*>> {
        val seen = mutableListOf<LoadingResult<*>>()
        repository.recipeDetails(id).test {
            while (true) {
                val result = awaitItem().getOrNull() ?: continue
                seen += result
                if (result is LoadingResult.Loaded) break
            }
            cancelAndIgnoreRemainingEvents()
        }
        return seen
    }

    @Test
    fun uncachedRecipeIsFetchedWithALoadingState() = runTest {
        val seen = results()
        assertThat(seen.first()).isEqualTo(LoadingResult.Loading)
        assertThat(detailRequests).isEqualTo(1)
    }

    @Test
    fun cachedRecipeWithinTheTtlIsNotRefetched() = runTest {
        results()
        val seen = results()
        assertThat(seen.single()).isInstanceOf<LoadingResult.Loaded<*>>()
        assertThat(detailRequests).isEqualTo(1)
    }

    @Test
    fun staleCachedRecipeShowsCachedDataAndRefetches() = runTest {
        results()
        clock.now = start + CacheTtl.RECIPE_DETAIL

        repository.recipeDetails("recipe-1").test {
            val seen = mutableListOf<LoadingResult<*>>()
            while (!(seen.any { it == LoadingResult.RefreshComplete } && seen.any { it is LoadingResult.Loaded<*> })) {
                awaitItem().getOrNull()?.let { seen += it }
            }
            assertThat(seen.any { it == LoadingResult.Refreshing }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(detailRequests).isEqualTo(2)
    }
}
