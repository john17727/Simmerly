package dev.juanrincon.simmerly.recipes.data.remote

import app.tracktion.core.domain.util.DataError
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing.UserRatingUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.time.Instant

class RecipeNetworkClientTest {

    private var capturedRequest: HttpRequestData? = null

    private fun buildClient(
        statusCode: HttpStatusCode = HttpStatusCode.OK,
        body: String = RECIPE_LIST_JSON
    ): RecipeNetworkClient {
        val engine = MockEngine { req ->
            capturedRequest = req
            respond(
                content = body,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        return RecipeNetworkClient(HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })
    }

    // region getRecipes — URL and parameters

    @Test
    fun getRecipesWithoutNextHitsRecipesEndpoint() = runTest {
        buildClient().getRecipes(next = null)
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/recipes")
    }

    @Test
    fun getRecipesWithoutNextSendsPageAndPerPageParams() = runTest {
        buildClient().getRecipes(next = null)
        val params = capturedRequest!!.url.parameters
        assertThat(params["page"]).isEqualTo("1")
        assertThat(params["perPage"]).isEqualTo("50")
    }

    @Test
    fun getRecipesWithNextUrlUsesNextAsPath() = runTest {
        buildClient().getRecipes(next = "recipes?page=2")
        assertThat(capturedRequest!!.url.toString()).contains("recipes")
    }

    // endregion

    // region getRecipes — response handling

    @Test
    fun getRecipesSuccessReturnsItemListDto() = runTest {
        val result = buildClient(body = RECIPE_LIST_JSON).getRecipes(next = null)
        assertThat(result.isRight()).isEqualTo(true)
        assertThat(result.getOrNull()!!.items.first().name).isEqualTo("Test Recipe")
    }

    @Test
    fun getRecipesWith401ReturnsUnauthorized() = runTest {
        val result = buildClient(HttpStatusCode.Unauthorized, "").getRecipes(next = null)
        assertThat(result.isLeft()).isEqualTo(true)
        assertThat(result.leftOrNull()!!).isInstanceOf(DataError.NetworkError.Unauthorized::class)
    }

    @Test
    fun getRecipesWith500ReturnsServerError() = runTest {
        val result = buildClient(HttpStatusCode.InternalServerError, "").getRecipes(next = null)
        assertThat(result.isLeft()).isEqualTo(true)
        assertThat(result.leftOrNull()!!).isInstanceOf(DataError.NetworkError.ServerError::class)
    }

    // endregion

    // region getRecipe

    @Test
    fun getRecipeHitsCorrectEndpoint() = runTest {
        buildClient(body = RECIPE_DETAIL_JSON).getRecipe("test-slug")
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/recipes/test-slug")
    }

    @Test
    fun getRecipeSuccessReturnsRecipeDetailDto() = runTest {
        val result = buildClient(body = RECIPE_DETAIL_JSON).getRecipe("test-slug")
        assertThat(result.isRight()).isEqualTo(true)
        assertThat(result.getOrNull()!!.name).isEqualTo("Test Recipe")
    }

    // endregion

    // region addComment

    @Test
    fun addCommentHitsCommentsEndpoint() = runTest {
        buildClient(body = COMMENT_JSON).addComment("recipe-1", "Great!")
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/comments")
    }

    @Test
    fun addCommentSuccessReturnsCommentDto() = runTest {
        val result = buildClient(body = COMMENT_JSON).addComment("recipe-1", "Great!")
        assertThat(result.isRight()).isEqualTo(true)
        assertThat(result.getOrNull()!!.text).isEqualTo("Great!")
    }

    // endregion

    // region setRating

    @Test
    fun setRatingHitsUserRatingsEndpoint() = runTest {
        buildClient(body = "").setRating("user-1", "test-slug", 4.0)
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/users/user-1/ratings/test-slug")
    }

    @Test
    fun setRatingSendsTheRatingInTheRequestBody() = runTest {
        buildClient(body = "").setRating("user-1", "test-slug", 4.0)
        val sent = Json.decodeFromString<UserRatingUpdateDto>(capturedRequestBody())
        assertThat(sent.rating).isEqualTo(4.0)
        assertThat(sent.isFavorite).isNull()
    }

    @Test
    fun setRatingWithEmptyResponseBodyStillSucceeds() = runTest {
        // The spec declares this endpoint's success response as empty — this is the case
        // arrowNetworkHandlerNoContent exists for: never touching the body means an actually-empty
        // 2xx response can't fail to decode.
        val result = buildClient(body = "").setRating("user-1", "test-slug", 4.0)
        assertThat(result.isRight()).isEqualTo(true)
    }

    // endregion

    // region updateLastMade

    @Test
    fun updateLastMadeHitsLastMadeEndpoint() = runTest {
        buildClient(body = RECIPE_DETAIL_JSON).updateLastMade("test-slug", Instant.parse("2026-08-07T18:00:00Z"))
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/recipes/test-slug/last-made")
    }

    @Test
    fun updateLastMadeDecodesTheFullRecipeFromTheResponse() = runTest {
        // Despite the spec declaring an empty response, the real one returns the full recipe
        // object — confirmed against a live instance — so this is decoded exactly like patchRecipe.
        val result = buildClient(body = RECIPE_DETAIL_JSON)
            .updateLastMade("test-slug", Instant.parse("2026-08-07T18:00:00Z"))
        assertThat(result.isRight()).isEqualTo(true)
        assertThat(result.getOrNull()!!.name).isEqualTo("Test Recipe")
    }

    // endregion

    // region createTimelineEvent

    @Test
    fun createTimelineEventHitsTimelineEventsEndpoint() = runTest {
        buildClient(body = RECIPE_TIMELINE_EVENT_JSON)
            .createTimelineEvent("recipe-1", "Cooked", "Great recipe", TIMESTAMP)
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/recipes/timeline/events")
    }

    @Test
    fun createTimelineEventSendsExactlyTheFieldsMealiesOwnUiSends() = runTest {
        // Captured from Mealie's own web UI: recipeId, subject, eventType, eventMessage,
        // timestamp — and notably *no* userId or image, which the server infers/defaults.
        buildClient(body = RECIPE_TIMELINE_EVENT_JSON)
            .createTimelineEvent("recipe-1", "Juan Rincon made this", "Too good!", TIMESTAMP)

        val sent = Json.parseToJsonElement(capturedRequestBody()).jsonObject
        assertThat(sent.keys).isEqualTo(
            setOf("recipeId", "subject", "eventType", "eventMessage", "timestamp")
        )
        assertThat(sent["recipeId"]!!.jsonPrimitive.content).isEqualTo("recipe-1")
        assertThat(sent["subject"]!!.jsonPrimitive.content).isEqualTo("Juan Rincon made this")
        assertThat(sent["eventType"]!!.jsonPrimitive.content).isEqualTo("comment")
        assertThat(sent["eventMessage"]!!.jsonPrimitive.content).isEqualTo("Too good!")
        assertThat(sent["timestamp"]!!.jsonPrimitive.content).isEqualTo("2026-08-08T03:59:59Z")
    }

    @Test
    fun createTimelineEventWithNoNoteOmitsTheMessage() = runTest {
        buildClient(body = RECIPE_TIMELINE_EVENT_JSON)
            .createTimelineEvent("recipe-1", "Cooked", null, TIMESTAMP)
        val sent = Json.parseToJsonElement(capturedRequestBody()).jsonObject
        assertThat(sent.containsKey("eventMessage")).isEqualTo(false)
    }

    @Test
    fun createTimelineEventSuccessReturnsTheEvent() = runTest {
        val result = buildClient(body = RECIPE_TIMELINE_EVENT_JSON)
            .createTimelineEvent("recipe-1", "Cooked", "Great recipe", TIMESTAMP)
        assertThat(result.isRight()).isEqualTo(true)
        assertThat(result.getOrNull()!!.subject).isEqualTo("Cooked")
    }

    @Test
    fun createTimelineEventDecodesTheImageField() = runTest {
        // Confirms RecipeTimelineEventOutDto models the full response — Mealie always includes
        // `image` (one of its own literal strings, not actual image data) even when none was sent.
        val result = buildClient(body = RECIPE_TIMELINE_EVENT_JSON)
            .createTimelineEvent("recipe-1", "Cooked", "Great recipe", TIMESTAMP)
        assertThat(result.getOrNull()!!.image).isEqualTo("does not have image")
    }

    // endregion

    private fun capturedRequestBody(): String = (capturedRequest!!.body as TextContent).text

    companion object {
        val TIMESTAMP: Instant = Instant.parse("2026-08-08T03:59:59Z")

        val RECIPE_LIST_JSON = """
            {
              "page": 1, "per_page": 50, "total": 1, "total_pages": 1,
              "next": null, "previous": null,
              "items": [{
                "id": "recipe-1", "userId": "u1", "householdId": "h1", "groupId": "g1",
                "name": "Test Recipe", "slug": "test-recipe", "image": "",
                "recipeServings": 4.0, "recipeYieldQuantity": 4.0, "recipeYield": "",
                "totalTime": "30 minutes", "description": "",
                "recipeCategory": [], "tags": [], "tools": [],
                "orgURL": "", "dateAdded": "2024-01-01",
                "dateUpdated": "2024-01-01T00:00:00Z",
                "createdAt": "2024-01-01T00:00:00Z",
                "updatedAt": "2024-01-01T00:00:00Z"
              }]
            }
        """.trimIndent()

        val RECIPE_DETAIL_JSON = """
            {
              "id": "recipe-1", "userId": "u1", "householdId": "h1", "groupId": "g1",
              "name": "Test Recipe", "slug": "test-recipe", "image": "",
              "recipeServings": 4.0, "recipeYieldQuantity": 4.0, "recipeYield": "",
              "totalTime": "30 minutes", "description": "",
              "recipeCategory": [], "tags": [], "tools": [],
              "rating": null, "orgURL": "",
              "prepTime": null, "cookTime": null, "performTime": null,
              "dateAdded": "2024-01-01",
              "dateUpdated": "2024-01-01T00:00:00Z",
              "createdAt": "2024-01-01T00:00:00Z",
              "updatedAt": "2024-01-01T00:00:00Z",
              "lastMade": null,
              "recipeIngredient": [], "recipeInstructions": [],
              "nutrition": {
                "calories": null, "carbohydrateContent": null, "cholesterolContent": null,
                "fatContent": null, "fiberContent": null, "proteinContent": null,
                "saturatedFatContent": null, "sodiumContent": null, "sugarContent": null,
                "transFatContent": null, "unsaturatedFatContent": null
              },
              "settings": {
                "public": true, "showNutrition": false, "showAssets": false,
                "landscapeView": false, "disableComments": false, "locked": false
              },
              "assets": [], "notes": [], "comments": []
            }
        """.trimIndent()

        // Shape confirmed against a live instance — Mealie always includes `image`, even when the
        // request never set one.
        val RECIPE_TIMELINE_EVENT_JSON = """
            {
              "id": "event-1", "recipeId": "recipe-1", "userId": "user-1",
              "subject": "Cooked", "eventType": "comment", "eventMessage": "Great recipe",
              "image": "does not have image",
              "timestamp": "2026-08-07T18:00:00Z",
              "groupId": "g1", "householdId": "h1",
              "createdAt": "2026-08-07T18:00:00Z", "updatedAt": "2026-08-07T18:00:00Z"
            }
        """.trimIndent()

        val COMMENT_JSON = """
            {
              "recipeId": "recipe-1", "text": "Great!", "id": "comment-1",
              "createdAt": "2024-01-01T00:00:00Z", "updatedAt": "2024-01-01T00:00:00Z",
              "userId": "user-1",
              "user": {"id": "user-1", "username": "john", "admin": false, "fullName": "John Doe"}
            }
        """.trimIndent()
    }
}
