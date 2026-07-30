package dev.juanrincon.simmerly.recipes.data.local.recent

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.core.aRecipeEntity
import dev.juanrincon.simmerly.core.aRecentlyViewedEntity
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RecentlyViewedDaoTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var dao: RecentlyViewedDao

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        dao = db.recentlyViewedDao()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    // region observeWithRecipes — basic

    @Test
    fun observeWithRecipesEmitsEmptyListInitially() = runTest {
        dao.observeWithRecipes().test {
            assertThat(awaitItem()).hasSize(0)
        }
    }

    @Test
    fun observeWithRecipesReturnsRecipeAfterInsert() = runTest {
        // Write before subscribing — Room emits current state on first collection
        db.recipeDao().upsert(aRecipeEntity("recipe-1", name = "Pasta"))
        dao.upsert(aRecentlyViewedEntity(recipeId = "recipe-1"))

        dao.observeWithRecipes().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list.first().recipe.name).isEqualTo("Pasta")
        }
    }

    @Test
    fun observeWithRecipesDoesNotReturnEntryWithoutMatchingRecipe() = runTest {
        // INNER JOIN — no matching recipe means no result row
        dao.upsert(aRecentlyViewedEntity(recipeId = "ghost-recipe"))

        dao.observeWithRecipes().test {
            assertThat(awaitItem()).hasSize(0)
        }
    }

    // endregion

    // region ordering and limit

    @Test
    fun observeWithRecipesOrdersByViewedAtDescending() = runTest {
        db.recipeDao().upsert(aRecipeEntity("r1", name = "Old"))
        db.recipeDao().upsert(aRecipeEntity("r2", name = "New"))
        dao.upsert(aRecentlyViewedEntity(recipeId = "r1", viewedAt = 1000L))
        dao.upsert(aRecentlyViewedEntity(recipeId = "r2", viewedAt = 2000L))

        dao.observeWithRecipes().test {
            val list = awaitItem()
            assertThat(list.first().recipe.name).isEqualTo("New")
            assertThat(list.last().recipe.name).isEqualTo("Old")
        }
    }

    @Test
    fun observeWithRecipesReturnsAtMostTenItems() = runTest {
        repeat(15) { i ->
            db.recipeDao().upsert(aRecipeEntity("r$i"))
            dao.upsert(aRecentlyViewedEntity(recipeId = "r$i", viewedAt = i.toLong()))
        }

        dao.observeWithRecipes().test {
            assertThat(awaitItem()).hasSize(10)
        }
    }

    // endregion

    // region upsert (deduplication)

    @Test
    fun upsertWithSameRecipeIdKeepsOnlyOneEntry() = runTest {
        db.recipeDao().upsert(aRecipeEntity("recipe-1"))
        dao.upsert(aRecentlyViewedEntity(recipeId = "recipe-1", viewedAt = 1000L))
        dao.upsert(aRecentlyViewedEntity(recipeId = "recipe-1", viewedAt = 9999L))

        dao.observeWithRecipes().test {
            assertThat(awaitItem()).hasSize(1)
        }
    }

    // endregion

    // region reactive updates

    @Test
    fun observeWithRecipesEmitsUpdatedListWhenNewEntryAdded() = runTest {
        db.recipeDao().upsert(aRecipeEntity("recipe-1", name = "Pasta"))

        dao.observeWithRecipes().test {
            assertThat(awaitItem()).hasSize(0)
            dao.upsert(aRecentlyViewedEntity(recipeId = "recipe-1"))
            assertThat(awaitItem()).hasSize(1)
        }
    }

    // endregion
}
