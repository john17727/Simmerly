package dev.juanrincon.simmerly.recipes.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
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
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/** Ratings and favorites are applied locally first and rolled back when the server refuses. */
class SimmerlyRecipeRepositoryPreferencesTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var repository: SimmerlyRecipeRepository
    private var status = HttpStatusCode.NoContent

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        val client = RecipeNetworkClient(HttpClient(MockEngine { respond("", status) }) {
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

    private suspend fun seedRecipe() = db.recipeDao().upsert(aRecipeEntity(id = "recipe-1"))

    private suspend fun stored() = db.userRecipePreferenceDao().get("recipe-1")

    // region setRating

    @Test
    fun ratingIsKeptWhenTheServerAccepts() = runTest {
        seedRecipe()
        val result = repository.setRating("recipe-1", 4.0)
        assertThat(result.isRight()).isTrue()
        assertThat(stored()?.rating).isEqualTo(4.0)
    }

    @Test
    fun ratingKeepsTheExistingFavoriteFlag() = runTest {
        seedRecipe()
        db.userRecipePreferenceDao().upsert(UserRecipePreferenceEntity("recipe-1", rating = null, isFavorite = true))
        repository.setRating("recipe-1", 3.0)
        assertThat(stored()?.isFavorite).isEqualTo(true)
    }

    @Test
    fun rejectedRatingRestoresThePreviousPreference() = runTest {
        seedRecipe()
        db.userRecipePreferenceDao().upsert(UserRecipePreferenceEntity("recipe-1", rating = 2.0, isFavorite = true))
        status = HttpStatusCode.InternalServerError

        val result = repository.setRating("recipe-1", 5.0)

        assertThat(result.isLeft()).isTrue()
        assertThat(stored()).isEqualTo(UserRecipePreferenceEntity("recipe-1", rating = 2.0, isFavorite = true))
    }

    @Test
    fun rejectedRatingWithNoPreviousPreferenceLeavesNoRating() = runTest {
        seedRecipe()
        status = HttpStatusCode.InternalServerError

        repository.setRating("recipe-1", 5.0)

        assertThat(stored()?.rating).isNull()
        assertThat(stored()?.isFavorite).isEqualTo(false)
    }

    @Test
    fun ratingAnUnknownRecipeFailsWithoutWritingAnything() = runTest {
        val result = repository.setRating("missing", 4.0)
        assertThat(result.isLeft()).isTrue()
        assertThat(db.userRecipePreferenceDao().get("missing")).isNull()
    }

    // endregion

    // region setFavorite

    @Test
    fun favoriteIsKeptWhenTheServerAccepts() = runTest {
        seedRecipe()
        val result = repository.setFavorite("recipe-1", true)
        assertThat(result.isRight()).isTrue()
        assertThat(stored()?.isFavorite).isEqualTo(true)
    }

    @Test
    fun rejectedFavoriteRestoresThePreviousPreference() = runTest {
        seedRecipe()
        db.userRecipePreferenceDao().upsert(UserRecipePreferenceEntity("recipe-1", rating = 4.0, isFavorite = false))
        status = HttpStatusCode.InternalServerError

        repository.setFavorite("recipe-1", true)

        assertThat(stored()).isEqualTo(UserRecipePreferenceEntity("recipe-1", rating = 4.0, isFavorite = false))
    }

    @Test
    fun rejectedUnfavoriteWithNoStoredPreferenceDoesNotBecomeAFavorite() = runTest {
        // No stored row means "not a favorite"; rolling back must not invent one.
        seedRecipe()
        status = HttpStatusCode.InternalServerError

        repository.setFavorite("recipe-1", false)

        assertThat(stored()?.isFavorite ?: false).isFalse()
    }

    // endregion
}
