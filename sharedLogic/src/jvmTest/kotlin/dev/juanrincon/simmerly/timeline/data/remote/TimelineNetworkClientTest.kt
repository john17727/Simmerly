package dev.juanrincon.simmerly.timeline.data.remote

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.timeline.data.remote.dto.TimelineEventUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test

class TimelineNetworkClientTest {

    private val requests = mutableListOf<HttpRequestData>()
    private val json = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val eventJson = """{"id":"e1","recipeId":"r","userId":"u","subject":"s","eventType":"comment",
        "timestamp":"2026-11-21T10:00:00","groupId":"g","householdId":"h","createdAt":"t","updatedAt":"t"}"""

    private fun client(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        TimelineNetworkClient(HttpClient(MockEngine { req -> requests += req; handler(req) }) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = false }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })

    @Test
    fun getEventsAsksForThisRecipesInfoCommentAndSystemEventsNewestFirst() = runTest {
        val client = client {
            respond(
                """{"page":2,"per_page":32,"total":1,"total_pages":2,"items":[$eventJson],"next":null,"previous":"x"}""",
                HttpStatusCode.OK, json
            )
        }

        val page = client.getEvents("homemade-pizza", page = 2, perPage = 32)

        assertThat(page.getOrNull()!!.items.single().id).isEqualTo("e1")
        val url = requests.single().url
        assertThat(url.encodedPath).isEqualTo("/api/recipes/timeline/events")
        assertThat(url.parameters["page"]).isEqualTo("2")
        assertThat(url.parameters["perPage"]).isEqualTo("32")
        assertThat(url.parameters["orderBy"]).isEqualTo("timestamp")
        assertThat(url.parameters["orderDirection"]).isEqualTo("desc")
        val filter = url.parameters["queryFilter"]!!
        assertThat(filter).contains("recipe.slug=\"homemade-pizza\"")
        assertThat(filter).contains("eventType IN [\"info\", \"system\", \"comment\"]")
    }

    @Test
    fun updateSendsOnlyTheFieldsMealieAllowsToChange() = runTest {
        val client = client { respond(eventJson, HttpStatusCode.OK, json) }

        client.updateEvent("e1", TimelineEventUpdateDto(subject = "New", eventMessage = "msg"))

        val request = requests.single()
        assertThat(request.method).isEqualTo(HttpMethod.Put)
        assertThat(request.url.encodedPath).isEqualTo("/api/recipes/timeline/events/e1")
        val body = request.body.toString()
        assertThat(body.contains("userId")).isEqualTo(false)
    }

    @Test
    fun deleteUsesTheDeleteVerbAndNeedsNoBody() = runTest {
        val client = client { respond("", HttpStatusCode.OK) }

        assertThat(client.deleteEvent("e1").isRight()).isTrue()

        assertThat(requests.single().method).isEqualTo(HttpMethod.Delete)
        assertThat(requests.single().url.encodedPath).isEqualTo("/api/recipes/timeline/events/e1")
    }

    @Test
    fun uploadingAPhotoPutsAMultipartBodyToTheImageEndpoint() = runTest {
        val client = client { respond("{}", HttpStatusCode.OK, json) }

        val result = client.uploadImage("e1", byteArrayOf(1, 2, 3), "png")

        assertThat(result.isRight()).isTrue()
        val request = requests.single()
        assertThat(request.method).isEqualTo(HttpMethod.Put)
        assertThat(request.url.encodedPath).isEqualTo("/api/recipes/timeline/events/e1/image")
        assertThat(request.body.contentType.toString()).contains("multipart/form-data")
    }

    @Test
    fun membersAreRequestedWithoutPaging() = runTest {
        val client = client {
            respond(
                """{"page":1,"per_page":-1,"total":1,"total_pages":1,"next":null,"previous":null,
                "items":[{"id":"u","username":"juan","fullName":"Juan Rincon"}]}""", HttpStatusCode.OK, json
            )
        }

        val members = client.getMembers().getOrNull()!!

        assertThat(members.items.single().fullName).isEqualTo("Juan Rincon")
        assertThat(requests.single().url.parameters["perPage"]).isEqualTo("-1")
    }

    @Test
    fun serverFailuresAreMappedToNetworkErrors() = runTest {
        assertThat((client { respond("", HttpStatusCode.InternalServerError) }.deleteEvent("e1") as Either.Left).value)
            .isEqualTo(DataError.NetworkError.ServerError)
        assertThat((client { respond("", HttpStatusCode.Unauthorized) }.deleteEvent("e1") as Either.Left).value)
            .isEqualTo(DataError.NetworkError.Unauthorized)
        assertThat((client { respond("not json", HttpStatusCode.OK, json) }.getMembers() as Either.Left).value)
            .isInstanceOf(DataError.NetworkError::class)
    }
}
