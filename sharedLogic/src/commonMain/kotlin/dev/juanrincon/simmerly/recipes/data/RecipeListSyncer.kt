package dev.juanrincon.simmerly.recipes.data

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import arrow.core.Either
import arrow.core.raise.either
import dev.juanrincon.simmerly.core.data.CacheKeys
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.recipes.data.local.metadata.RecipeRemoteKey
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeCategoryCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeTagCrossRef
import dev.juanrincon.simmerly.recipes.data.mappers.toEntity
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * The one place a page of the recipe list is fetched and written to Room. Both the Paging
 * `RemoteMediator` (Android/desktop) and the repository's manual paging (iOS) go through it, so
 * the two can't drift apart.
 */
@OptIn(ExperimentalTime::class)
class RecipeListSyncer(
    private val networkClient: RecipeNetworkClient,
    private val database: SimmerlyDatabase,
    private val freshness: Freshness,
) {
    private val recipeDao = database.recipeDao()
    private val tagDao = database.tagDao()
    private val recipeTagDao = database.recipeTagDao()
    private val categoryDao = database.categoryDao()
    private val remoteKeyDao = database.recipeRemoteKeyDao()

    /** The cursor for the next page, or `null` when nothing is cached or the end was reached. */
    suspend fun nextCursor(): String? = remoteKeyDao.getKey()?.nextKey

    /** Whether any page has been synced since the cache was last emptied. */
    suspend fun hasSynced(): Boolean = remoteKeyDao.getKey() != null

    /**
     * Fetches the page at [cursor] and writes it in one transaction. A refresh (`cursor == null`)
     * replaces the cached list with the first page: rows not on it are deleted in the same
     * transaction as the upsert, so readers never see an empty table. Returns whether more pages
     * remain.
     */
    suspend fun sync(cursor: String?): Either<RecipesError, Boolean> = either {
        val isRefresh = cursor == null
        val response = networkClient.getRecipes(next = cursor, requireTags = true)
            .mapLeft { RecipesError.FetchError }.bind()

        val recipeIds = response.items.map { it.id }
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                if (isRefresh) recipeDao.deleteAllExcept(recipeIds)

                recipeDao.upsertAll(response.items.map { it.toEntity() })
                tagDao.upsertAll(response.items.flatMap { recipe -> recipe.tags.map { it.toEntity() } })
                categoryDao.upsertAll(
                    response.items.flatMap { recipe -> recipe.recipeCategory.map { it.toEntity() } }
                )

                // A recipe's tags/categories may have changed since it was cached, so its old
                // links are dropped before the current ones are written.
                recipeTagDao.clearForRecipes(recipeIds)
                categoryDao.clearCrossRefsForRecipes(recipeIds)
                recipeTagDao.insertAll(response.items.flatMap { recipe ->
                    recipe.tags.map { RecipeTagCrossRef(recipeId = recipe.id, tagId = it.id) }
                })
                categoryDao.insertCrossRefs(response.items.flatMap { recipe ->
                    recipe.recipeCategory.map { RecipeCategoryCrossRef(recipeId = recipe.id, categoryId = it.id) }
                })

                remoteKeyDao.upsert(
                    RecipeRemoteKey(id = REMOTE_KEY_ID, nextKey = response.next, createdAt = Clock.System.now())
                )
            }
        }
        // Only a first-page refresh makes the list as a whole fresh; appending older pages doesn't.
        if (isRefresh) freshness.markSynced(CacheKeys.RECIPE_LIST)

        response.next != null
    }

    private companion object {
        const val REMOTE_KEY_ID = "RECIPE_LIST"
    }
}
