package dev.juanrincon.simmerly.auth

import app.cash.turbine.test
import arrow.core.Either
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.auth.data.DefaultAuthRepository
import dev.juanrincon.simmerly.auth.data.network.AuthNetworkClient
import dev.juanrincon.simmerly.auth.domain.AuthState
import dev.juanrincon.simmerly.auth.domain.LoginError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAuthRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var sessionDataStore: FakeSessionDataStore

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionDataStore = FakeSessionDataStore()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildRepository(
        statusCode: HttpStatusCode = HttpStatusCode.OK,
        body: String = """{"access_token":"test-token","token_type":"bearer"}"""
    ): DefaultAuthRepository {
        val engine = MockEngine { _ ->
            respond(
                content = body,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return DefaultAuthRepository(sessionDataStore, AuthNetworkClient(httpClient))
    }

    // region observeAuthState

    @Test
    fun observeAuthStateDelegatesToSessionDataStore() = runTest {
        val repo = buildRepository()
        sessionDataStore.emitAuthState(AuthState.Authenticated)
        repo.observeAuthState().test {
            assertThat(awaitItem()).isEqualTo(AuthState.Authenticated)
        }
    }

    @Test
    fun observeAuthStateEmitsTransitionsFromSessionDataStore() = runTest {
        val repo = buildRepository()
        repo.observeAuthState().test {
            assertThat(awaitItem()).isEqualTo(AuthState.Loading) // initial
            sessionDataStore.emitAuthState(AuthState.Unauthenticated)
            assertThat(awaitItem()).isEqualTo(AuthState.Unauthenticated)
            sessionDataStore.emitAuthState(AuthState.Authenticated)
            assertThat(awaitItem()).isEqualTo(AuthState.Authenticated)
        }
    }

    // endregion

    // region login with API key

    @Test
    fun apiKeyLoginStoresServerAddressInSessionStore() = runTest {
        val repo = buildRepository()
        repo.login("https://myserver.com", "my-api-key")
        assertThat(sessionDataStore.serverAddress).isEqualTo("https://myserver.com")
    }

    @Test
    fun apiKeyLoginStoresApiKeyAsTokenInSessionStore() = runTest {
        val repo = buildRepository()
        repo.login("https://myserver.com", "my-api-key")
        assertThat(sessionDataStore.token).isEqualTo("my-api-key")
    }

    @Test
    fun apiKeyLoginReturnsSuccess() = runTest {
        val repo = buildRepository()
        val result = repo.login("https://myserver.com", "my-api-key")
        assertThat(result.isRight()).isTrue()
    }

    // endregion

    // region login with credentials — success

    @Test
    fun credentialsLoginOnSuccessStoresServerAddress() = runTest {
        val repo = buildRepository()
        repo.login("https://myserver.com", "user", "pass")
        assertThat(sessionDataStore.serverAddress).isEqualTo("https://myserver.com")
    }

    @Test
    fun credentialsLoginOnSuccessStoresTokenFromNetworkResponse() = runTest {
        val repo = buildRepository()
        repo.login("https://myserver.com", "user", "pass")
        assertThat(sessionDataStore.token).isEqualTo("test-token")
    }

    @Test
    fun credentialsLoginReturnsSuccessOnOkResponse() = runTest {
        val repo = buildRepository()
        val result = repo.login("https://myserver.com", "user", "pass")
        assertThat(result.isRight()).isTrue()
    }

    // endregion

    // region login with credentials — error mapping

    @Test
    fun credentialsLoginWith401ReturnsInvalidCredentials() = runTest {
        val repo = buildRepository(HttpStatusCode.Unauthorized, "")
        val result = repo.login("https://myserver.com", "user", "wrong-pass")
        assertThat(result).isEqualTo(Either.Left(LoginError.InvalidCredentials))
    }

    @Test
    fun credentialsLoginWith400ReturnsInvalidCredentials() = runTest {
        val repo = buildRepository(HttpStatusCode.BadRequest, "{}")
        val result = repo.login("https://myserver.com", "user", "wrong-pass")
        assertThat(result).isEqualTo(Either.Left(LoginError.InvalidCredentials))
    }

    @Test
    fun credentialsLoginWith500ReturnsNetworkError() = runTest {
        val repo = buildRepository(HttpStatusCode.InternalServerError, "")
        val result = repo.login("https://myserver.com", "user", "pass")
        assertThat(result).isEqualTo(Either.Left(LoginError.NetworkError))
    }

    // endregion
}
