package dev.juanrincon.simmerly.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import androidx.room3.RoomDatabase
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.aRecipeEntity
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.core.domain.LocalDataCleaner
import dev.juanrincon.simmerly.core.domain.ResettableCache
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListRepository
import kotlinx.coroutines.test.runTest
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.check.checkModules
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Builds the app's real module list — only the platform module is swapped, for an in-memory
 * database and a throwaway preferences file — so a missing binding, a wrong constructor argument
 * count or a dependency cycle fails here instead of at app start.
 */
class KoinGraphTest {

    private val tempDir = Files.createTempDirectory("simmerly-koin").toFile()

    private val testPlatformModule = module {
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.create(produceFile = { tempDir.resolve("session.preferences_pb") })
        }
        single<RoomDatabase.Builder<SimmerlyDatabase>> { Room.inMemoryDatabaseBuilder<SimmerlyDatabase>() }
    }

    private lateinit var app: KoinApplication

    @BeforeTest
    fun setUp() {
        app = koinApplication { modules(appModules(testPlatformModule)) }
    }

    @AfterTest
    fun tearDown() {
        app.koin.get<SimmerlyDatabase>().close()
        app.close()
        tempDir.deleteRecursively()
    }

    @Test
    fun everyDefinitionResolves() {
        // checkModules closes the application it checks, so this test builds its own. Definitions
        // that take parameters (a recipe id, a list id) are given a placeholder.
        koinApplication { modules(appModules(testPlatformModule)) }.checkModules {
            withParameter<String> { "id" }
        }
    }

    @Test
    fun recipeRepositoryIsTheSameInstanceUnderBothItsInterfaces() {
        val repository = app.koin.get<RecipeRepository>()
        val caches = app.koin.getAll<ResettableCache>()

        assertThat(caches.any { it === repository }).isTrue()
        assertThat(app.koin.get<RecipeRepository>()).isSameInstanceAs(repository)
    }

    @Test
    fun theLocalDataCleanerIsASingleton() {
        assertThat(app.koin.get<LocalDataCleaner>()).isSameInstanceAs(app.koin.get<LocalDataCleaner>())
    }

    @Test
    fun repositoriesThatTheCleanerAndTheHttpClientDependOnCanBeResolvedTogether() {
        // The authenticated HTTP client looks the cleaner up lazily; resolving all of these
        // together would loop if that lookup were eager.
        assertThat(app.koin.get<LocalDataCleaner>()).isNotNull()
        assertThat(app.koin.get<RecipeRepository>()).isNotNull()
        assertThat(app.koin.get<ShoppingListRepository>()).isNotNull()
        assertThat(app.koin.get<AuthRepository>()).isNotNull()
    }

    @Test
    fun loggingOutThroughTheRealGraphEmptiesTheDatabaseAndTheSession() = runTest {
        val database = app.koin.get<SimmerlyDatabase>()
        val session = app.koin.get<SessionDataStore>()
        val auth = app.koin.get<AuthRepository>()
        auth.login("https://mealie.example.com", "api-key")
        database.recipeDao().upsert(aRecipeEntity(id = "r1"))

        auth.logout()

        assertThat(database.recipeDao().existsById("r1")).isEqualTo(0)
        assertThat(session.getToken().orEmpty()).isEqualTo("")
    }
}
