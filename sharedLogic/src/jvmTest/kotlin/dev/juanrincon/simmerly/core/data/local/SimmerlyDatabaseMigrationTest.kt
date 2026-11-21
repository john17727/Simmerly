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

    private fun SQLiteConnection.columns(table: String): List<String> =
        prepare("PRAGMA table_info($table)").use { statement ->
            buildList {
                while (statement.step()) add(statement.getText(1))
            }
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
    fun migrating7To8DropsTheUnusedCreatedAtColumnAndKeepsTheCursor() = runTest {
        val helper = helper()
        helper.createDatabase(7).use {
            it.insertVersion5Data()
            it.execSQL("INSERT INTO recipe_remote_keys (id, nextKey, createdAt) VALUES ('RECIPE_LIST', 'recipes?page=2', '2026-01-01T00:00:00Z')")
        }

        val migrated = helper.runMigrationsAndValidate(8, emptyList())

        migrated.use {
            assertThat(it.columns("recipe_remote_keys")).isEqualTo(listOf("id", "nextKey"))
            val nextKey = it.prepare("SELECT nextKey FROM recipe_remote_keys WHERE id = 'RECIPE_LIST'").use { s ->
                s.step()
                s.getText(0)
            }
            assertThat(nextKey).isEqualTo("recipes?page=2")
            assertThat(it.count("recent_search_queries")).isEqualTo(1L)
        }
    }

    @Test
    fun migrating8To9DropsTheCookTimeColumnAndKeepsTheRecipe() = runTest {
        val helper = helper()
        helper.createDatabase(8).use {
            it.insertVersion5Data()
            it.execSQL(
                "INSERT INTO recipes (id, user_id, household_id, group_id, name, slug, image, servings, " +
                    "yield_quantity, yield, prep_time, cook_time, perform_time, description, original_url, " +
                    "date_added, date_updated, created_at, updated_at) VALUES ('r1', 'u1', 'h1', 'g1', " +
                    "'Pancakes', 'pancakes', '', 2.0, 0.0, '', '10 min', '15 min', '20 min', '', '', " +
                    "'2026-01-01', '2026-01-01', '2026-01-01', '2026-01-01')"
            )
        }

        val migrated = helper.runMigrationsAndValidate(9, emptyList())

        migrated.use {
            assertThat(it.columns("recipes").contains("cook_time")).isEqualTo(false)
            val times = it.prepare("SELECT prep_time, perform_time FROM recipes WHERE id = 'r1'").use { s ->
                s.step()
                s.getText(0) to s.getText(1)
            }
            assertThat(times).isEqualTo("10 min" to "20 min")
            assertThat(it.count("user_recipe_preferences")).isEqualTo(1L)
        }
    }

    @Test
    fun migratingStraightFrom5ToTheCurrentVersionRunsEveryMigration() = runTest {
        val helper = helper()
        helper.createDatabase(5).use { it.insertVersion5Data() }

        val migrated = helper.runMigrationsAndValidate(9, emptyList())

        migrated.use {
            assertThat(it.tableExists("cache_metadata")).isEqualTo(true)
            assertThat(it.tableExists("shopping_lists")).isEqualTo(true)
            assertThat(it.columns("recipe_remote_keys")).isEqualTo(listOf("id", "nextKey"))
            assertThat(it.columns("recipes").contains("cook_time")).isEqualTo(false)
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
    fun theProductionBuilderConfigurationMigratesInsteadOfResetting() = runTest {
        helper().createDatabase(5).use { it.insertVersion5Data() }

        // The app's own configuration has a destructive fallback, which would silently wipe the
        // data if the migrations were not registered correctly; surviving data proves they are.
        val db = getRoomDatabase(Room.databaseBuilder<SimmerlyDatabase>(name = dbPath.toString()))
        try {
            assertThat(db.userRecipePreferenceDao().get("r1")?.rating).isEqualTo(4.5)
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
