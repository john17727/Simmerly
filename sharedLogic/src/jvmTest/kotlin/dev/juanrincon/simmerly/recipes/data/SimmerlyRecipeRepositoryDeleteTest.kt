package dev.juanrincon.simmerly.recipes.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.auth.FakeSessionDataStore
import dev.juanrincon.simmerly.core.aRecipeEntity
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.initialload.FakeUserRepository
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserRecipePreferenceEntity
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/** A recipe leaves the local cache only once the server has confirmed the delete. */
class SimmerlyRecipeRepositoryDeleteTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var repository: SimmerlyRecipeRepository
    private var status = HttpStatusCode.OK
    private val requests = mutableListOf<Pair<HttpMethod, String>>()

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        val client = RecipeNetworkClient(HttpClient(MockEngine { req ->
            requests += req.method to req.url.encodedPath
            respond("", status)
        }) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { contentType(ContentType.Application.Json) }
        })
        repository = SimmerlyRecipeRepository(
            networkClient = client,
            database = db,
            sessionDataStore = FakeSessionDataStore(),
            userRepository = FakeUserRepository(),
            freshness = Freshness(db.cacheMetadataDao()),
        )
    }

    @AfterTest
    fun tearDown() {
        repository.reset()
        db.close()
    }

    private suspend fun seedRecipe() {
        db.recipeDao().upsert(aRecipeEntity(id = "recipe-1"))
        db.userRecipePreferenceDao().upsert(UserRecipePreferenceEntity("recipe-1", rating = 4.0, isFavorite = true))
    }

    @Test
    fun successDeletesTheRecipeAndItsPreferenceLocally() = runTest {
        seedRecipe()

        val result = repository.deleteRecipe("recipe-1")

        assertThat(result.isRight()).isTrue()
        assertThat(requests.single().first).isEqualTo(HttpMethod.Delete)
        assertThat(db.recipeDao().existsById("recipe-1")).isEqualTo(0)
        assertThat(db.userRecipePreferenceDao().get("recipe-1")).isNull()
    }

    @Test
    fun rejectedDeleteKeepsTheRecipe() = runTest {
        seedRecipe()
        status = HttpStatusCode.InternalServerError

        val result = repository.deleteRecipe("recipe-1")

        assertThat(result.leftOrNull()).isEqualTo(RecipesError.DeleteError)
        assertThat(db.recipeDao().existsById("recipe-1")).isEqualTo(1)
        assertThat(db.userRecipePreferenceDao().get("recipe-1")).isNotNull()
    }

    @Test
    fun deletingAnUnknownRecipeFailsWithoutCallingTheServer() = runTest {
        val result = repository.deleteRecipe("missing")

        assertThat(result.leftOrNull()).isEqualTo(RecipesError.DeleteError)
        assertThat(requests.isEmpty()).isTrue()
    }
}
