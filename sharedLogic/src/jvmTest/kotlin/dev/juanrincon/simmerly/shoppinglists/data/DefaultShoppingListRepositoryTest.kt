package dev.juanrincon.simmerly.shoppinglists.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.auth.FakeSessionDataStore
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UnitEntity
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingListStore
import dev.juanrincon.simmerly.shoppinglists.data.remote.ShoppingListNetworkClient
import dev.juanrincon.simmerly.shoppinglists.data.remote.ShoppingListNetworkClientTest
import dev.juanrincon.simmerly.shoppinglists.aFood
import dev.juanrincon.simmerly.shoppinglists.aRecipe
import dev.juanrincon.simmerly.shoppinglists.aShoppingList
import dev.juanrincon.simmerly.shoppinglists.aUnit
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

class DefaultShoppingListRepositoryTest {

    private lateinit var db: SimmerlyDatabase

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    private fun newRepository(client: ShoppingListNetworkClient) = DefaultShoppingListRepository(
        networkClient = client,
        sessionDataStore = FakeSessionDataStore(),
        store = ShoppingListStore(db, Freshness(db.cacheMetadataDao())),
        unitDao = db.unitDao(),
    )

    /** Serves the list for GETs; every write answers with [writeStatus] and [writeBody]. */
    private fun repository(
        writeStatus: HttpStatusCode = HttpStatusCode.OK,
        writeBody: String = ShoppingListNetworkClientTest.COLLECTION_JSON,
    ): DefaultShoppingListRepository {
        val engine = MockEngine { req ->
            val (status, body) = when (req.method) {
                HttpMethod.Get -> HttpStatusCode.OK to ShoppingListNetworkClientTest.LIST_JSON
                else -> writeStatus to writeBody
            }
            respond(body, status, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }
        val client = ShoppingListNetworkClient(HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })
        return newRepository(client)
    }

    @Test
    fun failedCheckRevertsTheOptimisticChange() = runTest {
        val repo = repository(writeStatus = HttpStatusCode.InternalServerError, writeBody = "{}")
        repo.refreshList("list-1")

        val result = repo.setChecked("list-1", setOf("item-1"), checked = true)

        assertThat(result.isLeft()).isTrue()
        val item = repo.observeList("list-1").first()!!.items.first { it.id == "item-1" }
        assertThat(item.checked).isEqualTo(false)
    }

    @Test
    fun failedDeleteRestoresTheItems() = runTest {
        val repo = repository(writeStatus = HttpStatusCode.InternalServerError, writeBody = "{}")
        repo.refreshList("list-1")

        repo.deleteItems("list-1", setOf("item-2"))

        val ids = repo.observeList("list-1").first()!!.items.map { it.id }
        assertThat(ids).containsExactly("item-1", "item-2")
    }

    @Test
    fun addFoodItemSendsTheFoodUnitAndTheFoodsAisle() = runTest {
        val requests = mutableListOf<io.ktor.client.request.HttpRequestData>()
        val engine = MockEngine { req ->
            requests += req
            val body = if (req.method == HttpMethod.Get) ShoppingListNetworkClientTest.LIST_JSON
            else ShoppingListNetworkClientTest.COLLECTION_JSON
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }
        val client = ShoppingListNetworkClient(HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })
        val repo = newRepository(client)
        repo.refreshList("list-1")

        repo.addFoodItem("list-1", aFood(id = "food-milk", labelId = "dairy"), 2.0, aUnit(id = "cup"), "oat")

        val body = Json.parseToJsonElement((requests.last().body as TextContent).text).jsonObject
        assertThat(body["foodId"]!!.jsonPrimitive.content).isEqualTo("food-milk")
        assertThat(body["unitId"]!!.jsonPrimitive.content).isEqualTo("cup")
        assertThat(body["labelId"]!!.jsonPrimitive.content).isEqualTo("dairy")
        assertThat(body["quantity"]!!.jsonPrimitive.content).isEqualTo("2.0")
        assertThat(body["note"]!!.jsonPrimitive.content).isEqualTo("oat")
        // Two items already on the list, so the new one goes after them.
        assertThat(body["position"]!!.jsonPrimitive.content).isEqualTo("1")
    }

    @Test
    fun itemWriteResultsAreFoldedIntoTheList() = runTest {
        val repo = repository(
            writeBody = """
            {"createdItems": [{"id": "item-3", "shoppingListId": "list-1", "note": "Coffee", "position": 2}],
             "updatedItems": [],
             "deletedItems": [{"id": "item-2", "shoppingListId": "list-1"}]}
            """
        )
        repo.refreshList("list-1")

        repo.addItem("list-1", "Coffee")

        val items = repo.observeList("list-1").first()!!.items
        assertThat(items.map { it.id }).containsExactly("item-1", "item-3")
        assertThat(items.last().note).isEqualTo("Coffee")
    }

    @Test
    fun listsSurviveANewRepositoryOverTheSameDatabase() = runTest {
        repository().refreshList("list-1")

        // A cold start: new repository, same database, and a server that can't be reached.
        val restarted = newRepository(offlineClient())

        val list = restarted.observeList("list-1").first()!!
        assertThat(list.items.map { it.id }).containsExactly("item-1", "item-2")
    }

    @Test
    fun unitsComeFromTheUnitsTableWithoutTheNetwork() = runTest {
        val now = Instant.parse("2024-01-01T00:00:00Z")
        db.unitDao().upsert(
            UnitEntity(
                id = "cup", name = "cup", pluralName = "cups", description = "", fraction = true,
                abbreviation = "c", pluralAbbreviation = null, useAbbreviation = false,
                createdAt = now, updatedAt = now,
            )
        )

        val units = newRepository(offlineClient()).units().getOrNull()!!

        assertThat(units.map { it.id }).containsExactly("cup")
        assertThat(units.single().pluralName).isEqualTo("cups")
    }

    @Test
    fun unitsFallBackToTheNetworkWhenTheTableIsEmpty() = runTest {
        val paths = mutableListOf<String>()
        val client = ShoppingListNetworkClient(HttpClient(MockEngine { req ->
            paths += req.url.encodedPath
            respond("{}", HttpStatusCode.ServiceUnavailable)
        }) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })

        newRepository(client).units()

        assertThat(paths).containsExactly("/api/units")
    }

    // region optimistic writes: rename, delete list, recipe quantity

    private fun seedStore() = ShoppingListStore(db, Freshness(db.cacheMetadataDao()))

    @Test
    fun renameIsKeptWhenTheServerAccepts() = runTest {
        val repo = repository(writeBody = ShoppingListNetworkClientTest.LIST_JSON)
        repo.refreshList("list-1")

        val result = repo.renameList("list-1", "Renamed")

        assertThat(result.isRight()).isTrue()
        assertThat(repo.observeList("list-1").first()!!.name).isEqualTo("Renamed")
    }

    @Test
    fun failedRenameRestoresTheName() = runTest {
        val repo = repository(writeStatus = HttpStatusCode.InternalServerError, writeBody = "{}")
        repo.refreshList("list-1")

        val result = repo.renameList("list-1", "Renamed")

        assertThat(result.isLeft()).isTrue()
        assertThat(repo.observeList("list-1").first()!!.name).isEqualTo("Sunday brunch")
    }

    @Test
    fun renamingAnUnknownListFailsWithoutCallingTheServer() = runTest {
        val result = repository().renameList("missing", "x")
        assertThat(result.isLeft()).isTrue()
    }

    @Test
    fun deletedListIsGoneWhenTheServerAccepts() = runTest {
        val repo = repository(writeStatus = HttpStatusCode.NoContent, writeBody = "")
        repo.refreshList("list-1")

        val result = repo.deleteList("list-1")

        assertThat(result.isRight()).isTrue()
        assertThat(repo.observeList("list-1").first()).isNull()
    }

    @Test
    fun failedDeleteListRestoresTheListWithItsItems() = runTest {
        val repo = repository(writeStatus = HttpStatusCode.InternalServerError, writeBody = "{}")
        repo.refreshList("list-1")

        val result = repo.deleteList("list-1")

        assertThat(result.isLeft()).isTrue()
        val restored = repo.observeList("list-1").first()!!
        assertThat(restored.items.map { it.id }).containsExactly("item-1", "item-2")
    }

    @Test
    fun failedRecipeQuantityChangeRestoresTheQuantity() = runTest {
        seedStore().upsert(aShoppingList(id = "list-1", recipes = listOf(aRecipe("recipe-9", quantity = 2.0))))
        val repo = repository(writeStatus = HttpStatusCode.InternalServerError, writeBody = "{}")

        val result = repo.changeRecipeQuantity("list-1", "recipe-9", delta = 1)

        assertThat(result.isLeft()).isTrue()
        assertThat(repo.observeList("list-1").first()!!.recipes.single().quantity).isEqualTo(2.0)
    }

    @Test
    fun recipeQuantityChangeTakesTheServersRecalculatedList() = runTest {
        seedStore().upsert(aShoppingList(id = "list-1", name = "stale", recipes = listOf(aRecipe("recipe-9"))))
        val repo = repository(writeBody = ShoppingListNetworkClientTest.LIST_JSON)

        val result = repo.changeRecipeQuantity("list-1", "recipe-9", delta = 1)

        assertThat(result.isRight()).isTrue()
        val list = repo.observeList("list-1").first()!!
        assertThat(list.name).isEqualTo("Sunday brunch")
        assertThat(list.items.map { it.id }).containsExactly("item-1", "item-2")
    }

    @Test
    fun recipeQuantityChangeOnAnUnknownListFails() = runTest {
        assertThat(repository().changeRecipeQuantity("missing", "recipe-9", delta = 1).isLeft()).isTrue()
    }

    // endregion

    // region refreshListsIfStale

    private fun countingClient(paths: MutableList<String>) = ShoppingListNetworkClient(HttpClient(MockEngine { req ->
        paths += req.url.encodedPath
        respond(
            """{"page":1,"per_page":-1,"total":0,"total_pages":1,"items":[],"next":null,"previous":null}""",
            HttpStatusCode.OK,
            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
    }) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        defaultRequest { contentType(ContentType.Application.Json) }
    })

    @Test
    fun neverSyncedListsAreRefreshed() = runTest {
        val paths = mutableListOf<String>()

        newRepository(countingClient(paths)).refreshListsIfStale()

        assertThat(paths).containsExactly("/api/households/shopping/lists")
    }

    @Test
    fun freshListsAreNotRefreshedAgain() = runTest {
        val paths = mutableListOf<String>()
        val repo = newRepository(countingClient(paths))
        repo.refreshLists()
        paths.clear()

        val result = repo.refreshListsIfStale()

        assertThat(result.isRight()).isTrue()
        assertThat(paths.isEmpty()).isTrue()
    }

    @Test
    fun anExplicitRefreshIgnoresFreshness() = runTest {
        val paths = mutableListOf<String>()
        val repo = newRepository(countingClient(paths))
        repo.refreshLists()
        paths.clear()

        repo.refreshLists()

        assertThat(paths).containsExactly("/api/households/shopping/lists")
    }

    // endregion

    private fun repository() = newRepository(
        ShoppingListNetworkClient(HttpClient(MockEngine { req ->
            val body = if (req.method == HttpMethod.Get) ShoppingListNetworkClientTest.LIST_JSON
            else ShoppingListNetworkClientTest.COLLECTION_JSON
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })
    )

    private fun offlineClient() = ShoppingListNetworkClient(HttpClient(MockEngine {
        respond("{}", HttpStatusCode.ServiceUnavailable)
    }) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        defaultRequest { contentType(ContentType.Application.Json) }
    })
}
