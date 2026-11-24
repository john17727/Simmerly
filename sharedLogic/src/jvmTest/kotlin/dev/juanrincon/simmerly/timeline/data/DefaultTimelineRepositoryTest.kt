package dev.juanrincon.simmerly.timeline.data

import app.cash.turbine.test
import arrow.core.Either
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.auth.FakeSessionDataStore
import dev.juanrincon.simmerly.core.aRecipeEntity
import dev.juanrincon.simmerly.core.aUserEntity
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.initialload.FakeUserRepository
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.timeline.data.remote.TimelineNetworkClient
import dev.juanrincon.simmerly.timeline.domain.PickedImage
import dev.juanrincon.simmerly.timeline.domain.TimelineError
import dev.juanrincon.simmerly.timeline.domain.TimelineEvent
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DefaultTimelineRepositoryTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var repository: DefaultTimelineRepository
    private val requests = mutableListOf<Triple<HttpMethod, String, String>>()
    private var failEvents = false

    private fun event(id: String, subject: String, ts: String, image: String = "does not have image") =
        """{"id":"$id","recipeId":"recipe-1","userId":"user-1","subject":"$subject","eventType":"comment",
        "eventMessage":"msg","image":"$image","timestamp":"$ts","groupId":"g","householdId":"h",
        "createdAt":"$ts","updatedAt":"$ts"}"""

    private fun page(vararg events: String, next: String? = null) =
        """{"page":1,"per_page":32,"total":${events.size},"total_pages":1,
        "items":[${events.joinToString(",")}],"next":${next?.let { "\"$it\"" } ?: "null"},"previous":null}"""

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        val client = TimelineNetworkClient(HttpClient(MockEngine { req ->
            requests += Triple(req.method, req.url.encodedPath, req.url.encodedQuery)
            val json = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            when {
                req.url.encodedPath.endsWith("/households/members") -> respond(
                    """{"page":1,"per_page":-1,"total":1,"total_pages":1,"next":null,"previous":null,
                    "items":[{"id":"user-1","username":"juan","fullName":"Juan Rincon"}]}""", HttpStatusCode.OK, json
                )
                req.method == HttpMethod.Get && failEvents -> respond("", HttpStatusCode.InternalServerError)
                req.method == HttpMethod.Get -> respond(
                    page(event("e2", "Second", "2026-11-22T10:00:00"), event("e1", "First", "2026-11-21T10:00:00")),
                    HttpStatusCode.OK, json
                )
                req.method == HttpMethod.Put && req.url.encodedPath.endsWith("/image") -> respond("{}", HttpStatusCode.OK, json)
                req.method == HttpMethod.Put -> respond(event("e1", "Renamed", "2026-11-21T10:00:00"), HttpStatusCode.OK, json)
                else -> respond("", HttpStatusCode.OK)
            }
        }) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })
        runBlocking { db.recipeDao().upsert(aRecipeEntity("recipe-1")); db.userDao().upsert(aUserEntity("user-1")) }
        repository = DefaultTimelineRepository(
            networkClient = client,
            timelineDao = db.timelineEventDao(),
            recipeDao = db.recipeDao(),
            userDao = db.userDao(),
            sessionDataStore = FakeSessionDataStore(),
            userRepository = FakeUserRepository(),
            freshness = Freshness(db.cacheMetadataDao()),
        )
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    private fun runBlocking(block: suspend () -> Unit) = kotlinx.coroutines.runBlocking { block() }

    private suspend fun loaded(): List<TimelineEvent> = repository.timeline("recipe-1")
        .mapNotNull { (it.getOrNull() as? LoadingResult.Loaded<List<TimelineEvent>>)?.data }
        .first()

    @Test
    fun anEmptyCacheIsFetchedAndShownNewestFirst() = runTest {
        val events = loaded()

        assertThat(events.map { it.id }).isEqualTo(listOf("e2", "e1"))
        // Same filter as Mealie's web UI, by the recipe's slug.
        val get = requests.first { it.first == HttpMethod.Get && it.second.endsWith("/events") }
        assertThat(get.third.contains("eventType")).isTrue()
        assertThat(get.third.contains("orderDirection=desc")).isTrue()
    }

    @Test
    fun theAuthorsNameComesFromTheHouseholdMembers() = runTest {
        assertThat(loaded().first().authorName).isEqualTo("Juan Rincon")
    }

    @Test
    fun aFailedFirstFetchReportsAnErrorInsteadOfAnEmptyTimeline() = runTest {
        failEvents = true

        repository.timeline("recipe-1").test {
            assertThat(awaitItem()).isEqualTo(Either.Right(LoadingResult.Loading))
            assertThat(awaitItem()).isEqualTo(Either.Left(TimelineError.FetchError))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updatingAnEntrySavesItLocallyAndUploadsTheNewPhoto() = runTest {
        loaded()

        val result = repository.updateEvent(
            "recipe-1", "e1", "Renamed", "msg", PickedImage(byteArrayOf(1, 2), "jpg"), removeImage = false
        )

        assertThat(result.isRight()).isTrue()
        assertThat(requests.any { it.first == HttpMethod.Put && it.second.endsWith("/events/e1/image") }).isTrue()
        val stored = loaded().first { it.id == "e1" }
        assertThat(stored.subject).isEqualTo("Renamed")
        assertThat(stored.image == null).isFalse()
    }

    @Test
    fun anEntryLeavesTheCacheOnlyOnceTheServerConfirmedTheDelete() = runTest {
        loaded()

        assertThat(repository.deleteEvent("recipe-1", "e1").isRight()).isTrue()

        assertThat(loaded()).hasSize(1)
        assertThat(requests.any { it.first == HttpMethod.Delete && it.second.endsWith("/events/e1") }).isTrue()
    }
}
