package dev.juanrincon.simmerly.initialload.data.remote

import app.tracktion.core.domain.util.DataError
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test

class AuxiliaryNetworkClientTest {

    private var capturedRequest: HttpRequestData? = null

    private fun buildClient(
        statusCode: HttpStatusCode = HttpStatusCode.OK,
        body: String = TAG_LIST_JSON
    ): AuxiliaryNetworkClient {
        val engine = MockEngine { req ->
            capturedRequest = req
            respond(
                content = body,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        return AuxiliaryNetworkClient(HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        })
    }

    // region Endpoint paths

    @Test
    fun getTagsHitsTagsEndpoint() = runTest {
        buildClient(body = TAG_LIST_JSON).getTags()
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/organizers/tags")
    }

    @Test
    fun getCategoriesHitsCategoriesEndpoint() = runTest {
        buildClient(body = CATEGORY_LIST_JSON).getCategories()
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/organizers/categories")
    }

    @Test
    fun getToolsHitsToolsEndpoint() = runTest {
        buildClient(body = TOOL_LIST_JSON).getTools()
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/organizers/tools")
    }

    @Test
    fun getUnitsHitsUnitsEndpoint() = runTest {
        buildClient(body = UNIT_LIST_JSON).getUnits()
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/units")
    }

    // endregion

    // region perPage=500 param

    @Test
    fun getTagsSendsPerPage500() = runTest {
        buildClient(body = TAG_LIST_JSON).getTags()
        assertThat(capturedRequest!!.url.parameters["perPage"]).isEqualTo("500")
    }

    @Test
    fun getCategoriesSendsPerPage500() = runTest {
        buildClient(body = CATEGORY_LIST_JSON).getCategories()
        assertThat(capturedRequest!!.url.parameters["perPage"]).isEqualTo("500")
    }

    @Test
    fun getToolsSendsPerPage500() = runTest {
        buildClient(body = TOOL_LIST_JSON).getTools()
        assertThat(capturedRequest!!.url.parameters["perPage"]).isEqualTo("500")
    }

    @Test
    fun getUnitsSendsPerPage500() = runTest {
        buildClient(body = UNIT_LIST_JSON).getUnits()
        assertThat(capturedRequest!!.url.parameters["perPage"]).isEqualTo("500")
    }

    // endregion

    // region Response handling

    @Test
    fun getTagsSuccessReturnsItemListDto() = runTest {
        val result = buildClient(body = TAG_LIST_JSON).getTags()
        assertThat(result.isRight()).isEqualTo(true)
        assertThat(result.getOrNull()!!.items.first().name).isEqualTo("Vegan")
    }

    @Test
    fun getTagsWith401ReturnsUnauthorized() = runTest {
        val result = buildClient(HttpStatusCode.Unauthorized, "").getTags()
        assertThat(result.isLeft()).isEqualTo(true)
        assertThat(result.leftOrNull()!!).isInstanceOf(DataError.NetworkError.Unauthorized::class)
    }

    @Test
    fun getTagsWith500ReturnsServerError() = runTest {
        val result = buildClient(HttpStatusCode.InternalServerError, "").getTags()
        assertThat(result.isLeft()).isEqualTo(true)
        assertThat(result.leftOrNull()!!).isInstanceOf(DataError.NetworkError.ServerError::class)
    }

    // endregion

    companion object {
        private fun wrapItems(items: String) = """
            {
              "page": 1, "per_page": 500, "total": 1, "total_pages": 1,
              "next": null, "previous": null,
              "items": [$items]
            }
        """.trimIndent()

        val TAG_LIST_JSON = wrapItems(
            """{"id": "t1", "groupId": "g1", "name": "Vegan", "slug": "vegan"}"""
        )
        val CATEGORY_LIST_JSON = wrapItems(
            """{"id": "c1", "groupId": "g1", "name": "Italian", "slug": "italian"}"""
        )
        val TOOL_LIST_JSON = wrapItems(
            """{"id": "tl1", "groupId": "g1", "name": "Blender", "slug": "blender"}"""
        )
        val UNIT_LIST_JSON = wrapItems("""
            {
              "id": "u1", "name": "cup", "pluralName": "cups", "description": "",
              "fraction": false, "abbreviation": "c", "pluralAbbreviation": "c",
              "useAbbreviation": true,
              "createdAt": "2024-01-01T00:00:00Z", "updatedAt": "2024-01-01T00:00:00Z"
            }
        """)
    }
}
