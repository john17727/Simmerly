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

class UserNetworkClientTest {

    private var capturedRequest: HttpRequestData? = null

    private fun buildClient(
        statusCode: HttpStatusCode = HttpStatusCode.OK,
        body: String = USER_JSON
    ): UserNetworkClient {
        val engine = MockEngine { req ->
            capturedRequest = req
            respond(
                content = body,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        return UserNetworkClient(HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        })
    }

    // region Endpoint paths

    @Test
    fun getSelfHitsSelfEndpoint() = runTest {
        buildClient().getSelf()
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/users/self")
    }

    @Test
    fun getSelfRatingsHitsRatingsEndpoint() = runTest {
        buildClient(body = RATINGS_JSON).getSelfRatings()
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/users/self/ratings")
    }

    @Test
    fun getSelfFavoritesHitsFavoritesEndpoint() = runTest {
        buildClient(body = RATINGS_JSON).getSelfFavorites()
        assertThat(capturedRequest!!.url.encodedPath).isEqualTo("/api/users/self/favorites")
    }

    // endregion

    // region Response handling

    @Test
    fun getSelfSuccessReturnsUserDto() = runTest {
        val result = buildClient(body = USER_JSON).getSelf()
        assertThat(result.isRight()).isEqualTo(true)
        assertThat(result.getOrNull()!!.username).isEqualTo("john")
    }

    @Test
    fun getSelfRatingsSuccessReturnsUserRatingsDto() = runTest {
        val result = buildClient(body = RATINGS_JSON).getSelfRatings()
        assertThat(result.isRight()).isEqualTo(true)
        assertThat(result.getOrNull()!!.ratings).isEqualTo(emptyList())
    }

    @Test
    fun getSelfWith401ReturnsUnauthorized() = runTest {
        val result = buildClient(HttpStatusCode.Unauthorized, "").getSelf()
        assertThat(result.isLeft()).isEqualTo(true)
        assertThat(result.leftOrNull()!!).isInstanceOf(DataError.NetworkError.Unauthorized::class)
    }

    @Test
    fun getSelfWith500ReturnsServerError() = runTest {
        val result = buildClient(HttpStatusCode.InternalServerError, "").getSelf()
        assertThat(result.isLeft()).isEqualTo(true)
        assertThat(result.leftOrNull()!!).isInstanceOf(DataError.NetworkError.ServerError::class)
    }

    // endregion

    companion object {
        val USER_JSON = """
            {"id": "user-1", "username": "john", "admin": false, "fullName": "John Doe"}
        """.trimIndent()

        val RATINGS_JSON = """{"ratings": []}""".trimIndent()
    }
}
