package dev.juanrincon.simmerly.recipes.data.local.recipe

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.core.aRecipeEntity
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RecipeDaoTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var dao: RecipeDao

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        dao = db.recipeDao()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    // region existsById

    @Test
    fun existsByIdReturnsZeroForMissingRecipe() = runTest {
        assertThat(dao.existsById("nonexistent")).isEqualTo(0)
    }

    @Test
    fun existsByIdReturnsOneAfterInsert() = runTest {
        dao.upsert(aRecipeEntity(id = "recipe-1"))
        assertThat(dao.existsById("recipe-1")).isEqualTo(1)
    }

    // endregion

    // region observeRecipeList — initial state

    @Test
    fun observeRecipeListEmitsEmptyListInitially() = runTest {
        dao.observeRecipeList().test {
            assertThat(awaitItem()).hasSize(0)
        }
    }

    // endregion

    // region upsert and observe

    @Test
    fun upsertStoresEntityAndIsReflectedInObserve() = runTest {
        dao.observeRecipeList().test {
            assertThat(awaitItem()).hasSize(0)
            dao.upsert(aRecipeEntity())
            assertThat(awaitItem()).hasSize(1)
        }
    }

    @Test
    fun upsertWithSameIdOverwritesExistingEntity() = runTest {
        dao.observeRecipeList().test {
            assertThat(awaitItem()).hasSize(0)
            dao.upsert(aRecipeEntity(id = "recipe-1", name = "Original"))
            awaitItem()
            dao.upsert(aRecipeEntity(id = "recipe-1", name = "Updated"))
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list.first().recipe.name).isEqualTo("Updated")
        }
    }

    @Test
    fun upsertAllStoresMultipleEntities() = runTest {
        dao.observeRecipeList().test {
            assertThat(awaitItem()).hasSize(0)
            dao.upsertAll(listOf(aRecipeEntity("r1"), aRecipeEntity("r2"), aRecipeEntity("r3")))
            assertThat(awaitItem()).hasSize(3)
        }
    }

    // endregion

    // region ordering

    @Test
    fun observeRecipeListOrdersByCreatedAtDescending() = runTest {
        val earlier = Clock.System.now() - kotlin.time.Duration.parse("1h")
        val later = Clock.System.now()
        // Write before subscribing — Room emits current state on first collection
        dao.upsert(aRecipeEntity(id = "old", name = "Old Recipe", createdAt = earlier))
        dao.upsert(aRecipeEntity(id = "new", name = "New Recipe", createdAt = later))

        dao.observeRecipeList().test {
            val list = awaitItem()
            assertThat(list.first().recipe.name).isEqualTo("New Recipe")
            assertThat(list.last().recipe.name).isEqualTo("Old Recipe")
        }
    }

    // endregion

    // region clearAll

    @Test
    fun clearAllRemovesAllRecipes() = runTest {
        dao.observeRecipeList().test {
            assertThat(awaitItem()).hasSize(0)
            dao.upsertAll(listOf(aRecipeEntity("r1"), aRecipeEntity("r2")))
            awaitItem()
            dao.clearAll()
            assertThat(awaitItem()).hasSize(0)
        }
    }

    // endregion

    // region delete

    @Test
    fun deleteRemovesSpecificRecipe() = runTest {
        val recipe = aRecipeEntity("recipe-1")
        dao.upsert(recipe)
        dao.upsert(aRecipeEntity("recipe-2"))
        dao.delete(recipe)

        assertThat(dao.existsById("recipe-1")).isEqualTo(0)
        assertThat(dao.existsById("recipe-2")).isEqualTo(1)
    }

    // endregion
}
