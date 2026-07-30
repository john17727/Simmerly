package dev.juanrincon.simmerly.recipes.data.remote

import app.tracktion.core.domain.util.DataError
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test

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

    companion object {
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
