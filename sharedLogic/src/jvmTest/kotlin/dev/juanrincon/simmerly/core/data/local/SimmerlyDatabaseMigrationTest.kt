package dev.juanrincon.simmerly.core.data.local

import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Runs the exported schemas (`schemas/.../N.json`) through real databases: a database is created
 * at an old version with data in it, migrated, and checked against the newer schema. Room fails
 * the migration if the result doesn't match the exported schema, so a schema change without a
 * matching migration breaks here rather than on a user's device.
 */
class SimmerlyDatabaseMigrationTest {

    private val dir: Path = Files.createTempDirectory("simmerly-migration")
    private val dbPath: Path = dir.resolve("test.db")

    @AfterTest
    fun tearDown() {
        @OptIn(kotlin.io.path.ExperimentalPathApi::class)
        dir.deleteRecursively()
    }

    private fun helper() = MigrationTestHelper(
        schemaDirectoryPath = Path.of("schemas"),
        databasePath = dbPath,
        driver = BundledSQLiteDriver(),
        databaseClass = SimmerlyDatabase::class,
        databaseFactory = { SimmerlyDatabaseConstructor.initialize() },
    )

    private fun SQLiteConnection.count(table: String): Long =
        prepare("SELECT COUNT(*) FROM $table").use { statement ->
            statement.step()
            statement.getLong(0)
        }

    private fun SQLiteConnection.tableExists(table: String): Boolean =
        prepare("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = '$table'").use {
            it.step()
            it.getLong(0) == 1L
        }

    /** Data that exists in every schema version: the recent-search table and a user preference. */
    private fun SQLiteConnection.insertVersion5Data() {
        execSQL("INSERT INTO recent_search_queries (`query`, searchedAt) VALUES ('soup', 1700000000000)")
        execSQL("INSERT INTO user_recipe_preferences (recipeId, rating, isFavorite) VALUES ('r1', 4.5, 1)")
    }

    @Test
    fun migrating5To6AddsTheCacheMetadataTableAndKeepsExistingData() = runTest {
        val helper = helper()
        helper.createDatabase(5).use { it.insertVersion5Data() }

        val migrated = helper.runMigrationsAndValidate(6, emptyList())

        migrated.use {
            assertThat(it.tableExists("cache_metadata")).isEqualTo(true)
            assertThat(it.count("cache_metadata")).isEqualTo(0L)
            assertThat(it.count("recent_search_queries")).isEqualTo(1L)
            assertThat(it.count("user_recipe_preferences")).isEqualTo(1L)
        }
    }

    @Test
    fun migrating6To7AddsTheShoppingListTablesAndKeepsExistingData() = runTest {
        val helper = helper()
        helper.createDatabase(6).use {
            it.insertVersion5Data()
            it.execSQL("INSERT INTO cache_metadata (`key`, lastSyncedAt) VALUES ('recipe_list', '2026-01-01T00:00:00Z')")
        }

        val migrated = helper.runMigrationsAndValidate(7, emptyList())

        migrated.use {
            listOf("shopping_lists", "shopping_list_items", "shopping_list_recipes").forEach { table ->
                assertThat(it.tableExists(table)).isEqualTo(true)
                assertThat(it.count(table)).isEqualTo(0L)
            }
            assertThat(it.count("cache_metadata")).isEqualTo(1L)
            assertThat(it.count("recent_search_queries")).isEqualTo(1L)
        }
    }

    @Test
    fun migratingStraightFrom5To7RunsBothMigrations() = runTest {
        val helper = helper()
        helper.createDatabase(5).use { it.insertVersion5Data() }

        val migrated = helper.runMigrationsAndValidate(7, emptyList())

        migrated.use {
            assertThat(it.tableExists("cache_metadata")).isEqualTo(true)
            assertThat(it.tableExists("shopping_lists")).isEqualTo(true)
            assertThat(it.count("user_recipe_preferences")).isEqualTo(1L)
        }
    }

    @Test
    fun aVersion5DatabaseOpensThroughTheRealBuilderWithoutLosingData() = runTest {
        helper().createDatabase(5).use { it.insertVersion5Data() }

        // No destructive fallback here: if a migration were missing this would throw, not reset.
        val db = Room.databaseBuilder<SimmerlyDatabase>(name = dbPath.toString())
            .setDriver(BundledSQLiteDriver())
            .build()
        try {
            val preference = db.userRecipePreferenceDao().get("r1")
            assertThat(preference?.rating).isEqualTo(4.5)
            assertThat(preference?.isFavorite).isEqualTo(true)
            db.cacheMetadataDao().get("anything")
            db.shoppingListDao().get("anything")
        } finally {
            db.close()
        }
    }

    @Test
    fun aSchemaThatWasNeverExportedCannotBeMigrated() = runTest {
        // Guards the test setup itself: a missing schema file must fail loudly, not pass silently.
        assertFailsWith<Throwable> { helper().createDatabase(99) }
    }
}
