package dev.juanrincon.simmerly.recipes.data.local.recipe

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.core.aCommentEntity
import dev.juanrincon.simmerly.core.aRecipeEntity
import dev.juanrincon.simmerly.core.aUserEntity
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
class CommentDaoTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var dao: CommentDao

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        dao = db.commentDao()
        runBlocking {
            db.recipeDao().upsert(aRecipeEntity("recipe-1"))
            db.recipeDao().upsert(aRecipeEntity("recipe-2"))
            db.userDao().upsert(aUserEntity("user-1"))
        }
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    // region upsert / upsertAll

    @Test
    fun upsertStoresComment() = runTest {
        dao.observeComments("recipe-1").test {
            assertThat(awaitItem()).hasSize(0)
            dao.upsert(aCommentEntity(id = "c1", recipeId = "recipe-1"))
            assertThat(awaitItem()).hasSize(1)
        }
    }

    @Test
    fun upsertAllStoresMultipleComments() = runTest {
        dao.observeComments("recipe-1").test {
            assertThat(awaitItem()).hasSize(0)
            dao.upsertAll(listOf(
                aCommentEntity("c1", recipeId = "recipe-1"),
                aCommentEntity("c2", recipeId = "recipe-1"),
                aCommentEntity("c3", recipeId = "recipe-1")
            ))
            val list = awaitItem()
            assertThat(list).hasSize(3)
        }
    }

    @Test
    fun upsertWithSameIdOverwritesComment() = runTest {
        dao.observeComments("recipe-1").test {
            assertThat(awaitItem()).hasSize(0)
            dao.upsert(aCommentEntity(id = "c1", recipeId = "recipe-1", text = "Original"))
            awaitItem()
            dao.upsert(aCommentEntity(id = "c1", recipeId = "recipe-1", text = "Updated"))
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list.first().comment.text).isEqualTo("Updated")
        }
    }

    // endregion

    // region observeComments ordering

    @Test
    fun observeCommentsOrdersByCreatedAtAscending() = runTest {
        val earlier = Clock.System.now() - kotlin.time.Duration.parse("1h")
        val later = Clock.System.now()
        // Write before subscribing — Room emits current state on first collection
        dao.upsert(aCommentEntity("c-late", createdAt = later, text = "Second"))
        dao.upsert(aCommentEntity("c-early", createdAt = earlier, text = "First"))

        dao.observeComments("recipe-1").test {
            val list = awaitItem()
            assertThat(list.first().comment.text).isEqualTo("First")
            assertThat(list.last().comment.text).isEqualTo("Second")
        }
    }

    // endregion

    // region deleteByRecipeId

    @Test
    fun deleteByRecipeIdRemovesOnlyThatRecipesComments() = runTest {
        dao.upsert(aCommentEntity("c1", recipeId = "recipe-1"))
        dao.upsert(aCommentEntity("c2", recipeId = "recipe-2"))
        dao.deleteByRecipeId("recipe-1")

        dao.observeComments("recipe-1").test {
            assertThat(awaitItem()).hasSize(0)
        }
        dao.observeComments("recipe-2").test {
            assertThat(awaitItem()).hasSize(1)
        }
    }

    @Test
    fun deleteByRecipeIdOnEmptyTableDoesNotCrash() = runTest {
        dao.deleteByRecipeId("recipe-1")
        dao.observeComments("recipe-1").test {
            assertThat(awaitItem()).hasSize(0)
        }
    }

    // endregion

    // region isolation between recipes

    @Test
    fun observeCommentsOnlyReturnsCommentsForThatRecipe() = runTest {
        dao.upsert(aCommentEntity("c1", recipeId = "recipe-1"))
        dao.upsert(aCommentEntity("c2", recipeId = "recipe-2"))

        dao.observeComments("recipe-1").test {
            assertThat(awaitItem()).hasSize(1)
        }
    }

    // endregion
}
