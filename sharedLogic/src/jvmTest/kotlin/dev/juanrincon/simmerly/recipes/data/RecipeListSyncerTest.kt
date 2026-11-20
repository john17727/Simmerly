package dev.juanrincon.simmerly.recipes.data

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.CacheKeys
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours

class RecipeListSyncerTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var freshness: Freshness
    private lateinit var syncer: RecipeListSyncer

    private var status = HttpStatusCode.OK
    private var body = page()

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        freshness = Freshness(db.cacheMetadataDao())
        val engine = MockEngine {
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = RecipeNetworkClient(HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })
        syncer = RecipeListSyncer(client, db, freshness)
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    private suspend fun cachedIds() = db.recipeDao().getRecipePage(0, 100).map { it.recipe.id }

    // region refresh

    @Test
    fun refreshWritesTheFirstPage() = runTest {
        body = page(recipe("a"), recipe("b"))
        syncer.sync(cursor = null)
        assertThat(cachedIds()).containsExactlyInAnyOrder("a", "b")
    }

    @Test
    fun refreshReportsWhetherMorePagesRemain() = runTest {
        body = page(recipe("a"), next = "recipes?page=2")
        assertThat(syncer.sync(null).getOrNull()).isEqualTo(true)
        body = page(recipe("a"), next = null)
        assertThat(syncer.sync(null).getOrNull()).isEqualTo(false)
    }

    @Test
    fun refreshDeletesRecipesNotOnTheNewPage() = runTest {
        body = page(recipe("a"), recipe("b"))
        syncer.sync(null)
        body = page(recipe("b"), recipe("c"))
        syncer.sync(null)
        assertThat(cachedIds()).containsExactlyInAnyOrder("b", "c")
    }

    @Test
    fun refreshKeepsRowsThatSurviveSoTheirRelationsAreNotCascadeDeleted() = runTest {
        // A delete-everything-then-insert refresh would cascade away a surviving recipe's cached
        // cross refs for an instant; here the survivor is only ever upserted.
        body = page(recipe("a", tags = listOf("t1")))
        syncer.sync(null)
        body = page(recipe("a", tags = listOf("t1")), recipe("b"))
        syncer.sync(null)
        val a = db.recipeDao().getRecipePage(0, 100).first { it.recipe.id == "a" }
        assertThat(a.tags.map { it.id }).isEqualTo(listOf("t1"))
    }

    @Test
    fun refreshReplacesTagsAndCategoriesThatChangedOnTheServer() = runTest {
        body = page(recipe("a", tags = listOf("t1", "t2"), categories = listOf("c1")))
        syncer.sync(null)
        body = page(recipe("a", tags = listOf("t2"), categories = listOf("c2")))
        syncer.sync(null)
        val a = db.recipeDao().getRecipePage(0, 100).single()
        assertThat(a.tags.map { it.id }).isEqualTo(listOf("t2"))
        assertThat(a.categories.map { it.id }).isEqualTo(listOf("c2"))
    }

    @Test
    fun refreshMarksTheListFresh() = runTest {
        assertThat(freshness.isStale(CacheKeys.RECIPE_LIST, 1.hours)).isTrue()
        syncer.sync(null)
        assertThat(freshness.isStale(CacheKeys.RECIPE_LIST, 1.hours)).isFalse()
    }

    @Test
    fun refreshStoresTheNextCursor() = runTest {
        body = page(recipe("a"), next = "recipes?page=2")
        syncer.sync(null)
        assertThat(syncer.hasSynced()).isTrue()
        assertThat(syncer.nextCursor()).isEqualTo("recipes?page=2")
    }

    // endregion

    // region append

    @Test
    fun appendKeepsEarlierPagesAndAdvancesTheCursor() = runTest {
        body = page(recipe("a"), next = "recipes?page=2")
        syncer.sync(null)
        body = page(recipe("b"), next = null)
        syncer.sync("recipes?page=2")
        assertThat(cachedIds()).containsExactlyInAnyOrder("a", "b")
        assertThat(syncer.nextCursor()).isNull()
    }

    @Test
    fun appendDoesNotMarkTheListFresh() = runTest {
        syncer.sync("recipes?page=2")
        assertThat(freshness.isStale(CacheKeys.RECIPE_LIST, 1.hours)).isTrue()
    }

    // endregion

    // region failure

    @Test
    fun failedRefreshReturnsLeftAndLeavesTheCacheUntouched() = runTest {
        body = page(recipe("a"))
        syncer.sync(null)

        status = HttpStatusCode.InternalServerError
        val result = syncer.sync(null)

        assertThat(result.isLeft()).isTrue()
        assertThat(cachedIds()).isEqualTo(listOf("a"))
    }

    @Test
    fun nothingIsSyncedBeforeTheFirstSuccess() = runTest {
        assertThat(syncer.hasSynced()).isFalse()
        assertThat(syncer.nextCursor()).isNull()
    }

    // endregion

    private companion object {
        fun recipe(
            id: String,
            tags: List<String> = emptyList(),
            categories: List<String> = emptyList(),
        ) = """
            {
              "id": "$id", "userId": "u1", "householdId": "h1", "groupId": "g1",
              "name": "Recipe $id", "slug": "$id", "image": "",
              "recipeServings": 4.0, "recipeYieldQuantity": 4.0, "recipeYield": "",
              "totalTime": "30 minutes", "description": "",
              "recipeCategory": [${categories.joinToString { """{"id":"$it","groupId":"g1","name":"$it","slug":"$it"}""" }}],
              "tags": [${tags.joinToString { """{"id":"$it","groupId":"g1","name":"$it","slug":"$it"}""" }}],
              "tools": [],
              "orgURL": "", "dateAdded": "2024-01-01",
              "dateUpdated": "2024-01-01T00:00:00Z",
              "createdAt": "2024-01-01T00:00:00Z",
              "updatedAt": "2024-01-01T00:00:00Z"
            }
        """.trimIndent()

        fun page(vararg recipes: String, next: String? = null) = """
            {
              "page": 1, "per_page": 50, "total": ${recipes.size}, "total_pages": 1,
              "next": ${next?.let { "\"$it\"" } ?: "null"}, "previous": null,
              "items": [${recipes.joinToString()}]
            }
        """.trimIndent()
    }
}
