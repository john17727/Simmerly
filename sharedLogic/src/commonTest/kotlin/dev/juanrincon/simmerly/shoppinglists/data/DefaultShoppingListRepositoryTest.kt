package dev.juanrincon.simmerly.shoppinglists.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.auth.FakeSessionDataStore
import dev.juanrincon.simmerly.shoppinglists.data.remote.ShoppingListNetworkClient
import dev.juanrincon.simmerly.shoppinglists.data.remote.ShoppingListNetworkClientTest
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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test

class DefaultShoppingListRepositoryTest {

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
        return DefaultShoppingListRepository(client, FakeSessionDataStore())
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
}
