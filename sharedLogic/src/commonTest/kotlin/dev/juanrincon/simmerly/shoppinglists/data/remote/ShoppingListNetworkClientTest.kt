package dev.juanrincon.simmerly.shoppinglists.data.remote

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing.RecipeIngredientDto
import dev.juanrincon.simmerly.shoppinglists.data.mappers.toUpdateDto
import dev.juanrincon.simmerly.shoppinglists.aRef
import dev.juanrincon.simmerly.shoppinglists.anItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

class ShoppingListNetworkClientTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun buildClient(vararg bodies: String): ShoppingListNetworkClient {
        var call = 0
        val engine = MockEngine { req ->
            requests += req
            respond(
                content = bodies[minOf(call++, bodies.lastIndex)],
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        return ShoppingListNetworkClient(HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })
    }

    private fun HttpRequestData.jsonBody() =
        Json.parseToJsonElement((body as TextContent).text)

    @Test
    fun getListDecodesNullTimesNotesAndMissingObjects() = runTest {
        val list = buildClient(LIST_JSON).getList("list-1").getOrNull()!!

        assertThat(requests.single().url.encodedPath).isEqualTo("/api/households/shopping/lists/list-1")
        assertThat(list.name).isEqualTo("Sunday brunch")
        assertThat(list.createdAt).isNull()
        assertThat(list.listItems.map { it.note }).containsExactly("diced", null)
        assertThat(list.listItems[0].food?.name).isEqualTo("yellow onion")
        assertThat(list.listItems[1].unit).isNull()
        assertThat(list.recipeReferences.single().recipe.name).isEqualTo("Shakshuka")
        assertThat(list.labelSettings.single().label.name).isEqualTo("Produce")
    }

    @Test
    fun getListsAsksForEveryListInOnePage() = runTest {
        buildClient(PAGE_JSON).getLists(page = 1, perPage = -1, orderBy = "name", orderDirection = "asc")

        val url = requests.single().url
        assertThat(url.encodedPath).isEqualTo("/api/households/shopping/lists")
        assertThat(url.parameters["perPage"]).isEqualTo("-1")
        assertThat(url.parameters["orderBy"]).isEqualTo("name")
    }

    @Test
    fun renameReSendsTheWholeListWithOnlyTheNameChanged() = runTest {
        buildClient(LIST_JSON, LIST_JSON).renameList("list-1", "Brunch")

        assertThat(requests.map { it.method }).containsExactly(HttpMethod.Get, HttpMethod.Put)
        val body = requests[1].jsonBody().jsonObject
        assertThat(body["name"]!!.jsonPrimitive.content).isEqualTo("Brunch")
        // Fields the client doesn't model still go back to the server untouched.
        assertThat(body["extras"] is JsonObject).isTrue()
        assertThat(body["listItems"]!!.jsonArray.size).isEqualTo(2)
    }

    @Test
    fun deleteItemsSendsEachIdAsItsOwnParameter() = runTest {
        buildClient("""{"message":"ok"}""").deleteItems(listOf("a", "b"))

        val request = requests.single()
        assertThat(request.method).isEqualTo(HttpMethod.Delete)
        assertThat(request.url.parameters.getAll("ids")).isEqualTo(listOf("a", "b"))
    }

    @Test
    fun bulkUpdateSendsEveryFieldEvenWhenItMatchesTheDefault() = runTest {
        val item = anItem(id = "i1", note = "Napkins", recipeRefs = listOf(aRef()))
        buildClient(COLLECTION_JSON).updateItems(listOf(item.toUpdateDto()))

        val request = requests.single()
        assertThat(request.method).isEqualTo(HttpMethod.Put)
        assertThat(request.url.encodedPath).isEqualTo("/api/households/shopping/items")
        val sent = request.jsonBody().jsonArray.single().jsonObject
        // `checked = false` is the server's default too; omitting it would still be a bug
        // the moment the item was checked.
        assertThat(sent.keys.containsAll(listOf("checked", "quantity", "position", "foodId", "unitId"))).isTrue()
        assertThat(sent["recipeReferences"]!!.jsonArray.single().jsonObject["shoppingListItemId"]!!.jsonPrimitive.content)
            .isEqualTo("i1")
    }

    @Test
    fun recipeServingsChangesHitTheAddAndDeleteEndpoints() = runTest {
        val client = buildClient(LIST_JSON)
        client.addRecipe("list-1", "recipe-1", quantity = 1.0)
        client.removeRecipe("list-1", "recipe-1", quantity = 1.0)

        assertThat(requests.map { it.url.encodedPath }).containsExactly(
            "/api/households/shopping/lists/list-1/recipe/recipe-1",
            "/api/households/shopping/lists/list-1/recipe/recipe-1/delete",
        )
        assertThat(requests[0].jsonBody().jsonObject["recipeIncrementQuantity"]!!.jsonPrimitive.content).isEqualTo("1.0")
        assertThat(requests[1].jsonBody().jsonObject["recipeDecrementQuantity"]!!.jsonPrimitive.content).isEqualTo("1.0")
    }

    @Test
    fun addRecipeSendsOnlyTheGivenIngredients() = runTest {
        val client = buildClient(LIST_JSON)
        val ingredient = RecipeIngredientDto(
            quantity = 2.0, unit = null, food = null, note = null, display = "2 eggs",
            title = null, originalText = null, referenceId = "ref-1",
        )
        client.addRecipe("list-1", "recipe-1", quantity = 1.5, ingredients = listOf(ingredient))

        val body = requests.single().jsonBody().jsonObject
        assertThat(body["recipeIncrementQuantity"]!!.jsonPrimitive.content).isEqualTo("1.5")
        assertThat(body["recipeIngredients"]!!.jsonArray.single().jsonObject["referenceId"]!!.jsonPrimitive.content)
            .isEqualTo("ref-1")
    }

    @Test
    fun parseIngredientPostsTheTextAndDecodesMatchedAndUnmatchedParts() = runTest {
        val parsed = buildClient(PARSED_JSON).parseIngredient("2 cups milk").getOrNull()!!

        val request = requests.single()
        assertThat(request.url.encodedPath).isEqualTo("/api/parser/ingredient")
        assertThat(request.jsonBody().jsonObject["ingredient"]!!.jsonPrimitive.content).isEqualTo("2 cups milk")
        assertThat(parsed.ingredient.quantity).isEqualTo(2.0)
        assertThat(parsed.ingredient.food?.id).isEqualTo("food-milk")
        // Mealie sends an unmatched unit as a bare name with no id.
        assertThat(parsed.ingredient.unit?.id).isNull()
        assertThat(parsed.ingredient.unit?.name).isEqualTo("cups")
    }

    companion object {
        const val PARSED_JSON = """
        {"input": "2 cups milk",
         "confidence": {"average": 0.9},
         "ingredient": {
           "quantity": 2.0,
           "unit": {"name": "cups", "description": ""},
           "food": {"id": "food-milk", "name": "milk", "labelId": "dairy",
                    "label": {"id": "dairy", "name": "Dairy", "color": "#959595", "groupId": "group-1"}},
           "note": "",
           "display": "2 cups milk",
           "referenceId": "ref"
         }}
        """

        const val LIST_JSON = """
        {
          "name": "Sunday brunch",
          "extras": {},
          "createdAt": null,
          "updatedAt": null,
          "groupId": "group-1",
          "userId": "user-1",
          "id": "list-1",
          "householdId": "household-1",
          "listItems": [
            {
              "quantity": 1.0,
              "unit": null,
              "food": {"id": "food-1", "name": "yellow onion", "pluralName": null, "description": "", "labelId": "label-1", "label": {"id": "label-1", "name": "Produce", "color": "#959595", "groupId": "group-1"}, "createdAt": null},
              "note": "diced",
              "display": "1 yellow onion diced",
              "shoppingListId": "list-1",
              "checked": false,
              "position": 0,
              "foodId": "food-1",
              "labelId": "label-1",
              "unitId": null,
              "extras": {},
              "id": "item-1",
              "groupId": "group-1",
              "householdId": "household-1",
              "label": {"id": "label-1", "name": "Produce", "color": "#959595", "groupId": "group-1"},
              "recipeReferences": [
                {"recipeId": "recipe-1", "recipeQuantity": 1.0, "recipeScale": 1.0, "recipeNote": null, "id": "ref-1", "shoppingListItemId": "item-1"}
              ],
              "createdAt": null,
              "updatedAt": null
            },
            {
              "quantity": 1.0,
              "note": null,
              "display": "Paper napkins",
              "shoppingListId": "list-1",
              "id": "item-2",
              "groupId": "group-1",
              "householdId": "household-1"
            }
          ],
          "recipeReferences": [
            {"id": "lref-1", "shoppingListId": "list-1", "recipeId": "recipe-1", "recipeQuantity": 1.0,
             "recipe": {"id": "recipe-1", "name": "Shakshuka", "slug": "shakshuka", "image": null, "totalTime": null}}
          ],
          "labelSettings": [
            {"shoppingListId": "list-1", "labelId": "label-1", "position": 0, "id": "ls-1",
             "label": {"id": "label-1", "name": "Produce", "color": "#959595", "groupId": "group-1"}}
          ]
        }
        """

        const val PAGE_JSON = """
        {"page": 1, "per_page": -1, "total": 0, "total_pages": 1, "items": [], "next": null, "previous": null}
        """

        const val COLLECTION_JSON = """{"createdItems": [], "updatedItems": [], "deletedItems": []}"""
    }
}
