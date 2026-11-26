package dev.juanrincon.simmerly.mealplan.data.remote

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.mealplan.data.remote.dto.PlanEntryInDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test

class MealPlanNetworkClientTest {

    private val requests = mutableListOf<HttpRequestData>()
    private val json = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val entryJson = """{"id":7,"groupId":"g","userId":"u","householdId":"h","date":"2026-11-26",
        "endDate":null,"entryType":"dinner","title":"","text":"","recipeId":null,"recipeScale":1.0}"""

    private fun client(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        MealPlanNetworkClient(HttpClient(MockEngine { req -> requests += req; handler(req) }) {
            // Same settings as the app's client: defaults are not sent.
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = false }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })

    private suspend fun HttpRequestData.bodyText() = (body as OutgoingContent.ByteArrayContent).bytes().decodeToString()

    @Test
    fun getEntriesAsksForTheRangeIncludingWeekEntries() = runTest {
        val client = client {
            respond(
                """{"page":1,"per_page":-1,"total":1,"total_pages":1,"items":[$entryJson],"next":null,"previous":null}""",
                HttpStatusCode.OK, json,
            )
        }

        val page = client.getEntries(LocalDate(2026, 11, 1), LocalDate(2026, 12, 12))

        assertThat(page.getOrNull()!!.items.single().id).isEqualTo(7)
        val url = requests.single().url
        assertThat(url.encodedPath).isEqualTo("/api/households/mealplans")
        assertThat(url.parameters["start_date"]).isEqualTo("2026-11-01")
        assertThat(url.parameters["end_date"]).isEqualTo("2026-12-12")
        assertThat(url.parameters["include_week_entries"]).isEqualTo("true")
        assertThat(url.parameters["perPage"]).isEqualTo("-1")
    }

    @Test
    fun updateSendsTheFullBodyIncludingNullsAndTheIds() = runTest {
        val client = client { respond(entryJson, HttpStatusCode.OK, json) }

        client.update(
            7,
            PlanEntryInDto(
                date = "2026-11-27", endDate = null, entryType = "dinner", title = "", text = "",
                recipeId = null, recipeScale = 1.0, id = 7, groupId = "g", userId = "u",
            ),
        )

        val request = requests.single()
        assertThat(request.method).isEqualTo(HttpMethod.Put)
        assertThat(request.url.encodedPath).isEqualTo("/api/households/mealplans/7")
        val body = request.bodyText()
        assertThat(body).contains("\"endDate\":null")
        assertThat(body).contains("\"title\":\"\"")
        assertThat(body).contains("\"recipeScale\":1.0")
        assertThat(body).contains("\"id\":7")
        assertThat(body).contains("\"groupId\":\"g\"")
    }

    @Test
    fun createLeavesTheIdsOut() = runTest {
        val client = client { respond(entryJson, HttpStatusCode.Created, json) }

        client.create(
            PlanEntryInDto("2026-11-26", null, "lunch", "", "", "r1", 2.0)
        )

        val request = requests.single()
        assertThat(request.method).isEqualTo(HttpMethod.Post)
        val body = request.bodyText()
        assertThat(body).contains("\"recipeId\":\"r1\"")
        assertThat(body.contains("groupId")).isEqualTo(false)
    }

    @Test
    fun deleteAndPreferencesAndRulesUseTheirEndpoints() = runTest {
        val client = client { req ->
            when {
                req.url.encodedPath.endsWith("preferences") -> respond("""{"firstDayOfWeek":1}""", HttpStatusCode.OK, json)
                req.url.encodedPath.endsWith("rules") -> respond(
                    """{"page":1,"per_page":-1,"total":1,"total_pages":1,"items":[{"id":"x","day":"unset","entryType":"dinner","queryFilterString":"a"}],"next":null,"previous":null}""",
                    HttpStatusCode.OK, json,
                )
                else -> respond("", HttpStatusCode.OK)
            }
        }

        client.delete(7)
        assertThat(client.getPreferences().getOrNull()!!.firstDayOfWeek).isEqualTo(1)
        assertThat(client.getRules().getOrNull()!!.items.single().day).isEqualTo("unset")

        assertThat(requests[0].method).isEqualTo(HttpMethod.Delete)
        assertThat(requests[0].url.encodedPath).isEqualTo("/api/households/mealplans/7")
    }
}
