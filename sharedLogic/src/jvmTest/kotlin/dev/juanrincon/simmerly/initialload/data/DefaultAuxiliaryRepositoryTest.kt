package dev.juanrincon.simmerly.initialload.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.CacheKeys
import dev.juanrincon.simmerly.core.data.CacheTtl
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.initialload.data.remote.AuxiliaryNetworkClient
import dev.juanrincon.simmerly.initialload.data.remote.AuxiliaryNetworkClientTest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant

class DefaultAuxiliaryRepositoryTest {

    private class FakeClock(var now: Instant) : Clock {
        override fun now(): Instant = now
    }

    private val start = Instant.fromEpochSeconds(1_000_000)
    private lateinit var db: SimmerlyDatabase
    private lateinit var clock: FakeClock
    private lateinit var freshness: Freshness
    private lateinit var repository: DefaultAuxiliaryRepository

    private var requestCount = 0
    private var status = HttpStatusCode.OK

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        clock = FakeClock(start)
        freshness = Freshness(db.cacheMetadataDao(), clock)
        val engine = MockEngine { request ->
            requestCount++
            val body = when {
                request.url.encodedPath.endsWith("/tags") -> AuxiliaryNetworkClientTest.TAG_LIST_JSON
                request.url.encodedPath.endsWith("/categories") -> AuxiliaryNetworkClientTest.CATEGORY_LIST_JSON
                request.url.encodedPath.endsWith("/tools") -> AuxiliaryNetworkClientTest.TOOL_LIST_JSON
                else -> AuxiliaryNetworkClientTest.UNIT_LIST_JSON
            }
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        repository = DefaultAuxiliaryRepository(
            networkClient = AuxiliaryNetworkClient(HttpClient(engine) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }),
            tagDao = db.tagDao(),
            categoryDao = db.categoryDao(),
            toolDao = db.toolDao(),
            foodDao = db.foodDao(),
            unitDao = db.unitDao(),
            freshness = freshness,
        )
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun firstLoadFetchesAndMarksTheDatasetFresh() = runTest {
        val result = repository.loadTags()
        assertThat(result.isRight()).isTrue()
        assertThat(requestCount).isEqualTo(1)
        assertThat(freshness.isStale(CacheKeys.REFERENCE_TAGS, CacheTtl.REFERENCE_DATA)).isFalse()
    }

    @Test
    fun loadWithinTheTtlSkipsTheNetwork() = runTest {
        repository.loadTags()
        val second = repository.loadTags()
        assertThat(second.isRight()).isTrue()
        assertThat(requestCount).isEqualTo(1)
    }

    @Test
    fun loadAfterTheTtlFetchesAgain() = runTest {
        repository.loadUnits()
        clock.now = start + CacheTtl.REFERENCE_DATA
        repository.loadUnits()
        assertThat(requestCount).isEqualTo(2)
    }

    @Test
    fun failedLoadStaysStaleAndIsRetried() = runTest {
        status = HttpStatusCode.InternalServerError
        assertThat(repository.loadCategories().isLeft()).isTrue()
        assertThat(freshness.isStale(CacheKeys.REFERENCE_CATEGORIES, CacheTtl.REFERENCE_DATA)).isTrue()

        status = HttpStatusCode.OK
        assertThat(repository.loadCategories().isRight()).isTrue()
        assertThat(requestCount).isEqualTo(2)
    }

    @Test
    fun datasetsAreTrackedIndependently() = runTest {
        repository.loadTags()
        repository.loadTools()
        assertThat(requestCount).isEqualTo(2)
    }
}
