package dev.juanrincon.simmerly.recipes.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import dev.juanrincon.simmerly.recipes.data.local.metadata.RecipeRemoteKey
import dev.juanrincon.simmerly.recipes.data.local.metadata.RecipeRemoteKeyDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeTagDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.TagDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeTagCrossRef
import dev.juanrincon.simmerly.recipes.data.mappers.toEntity
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalPagingApi::class, ExperimentalTime::class)
class RecipeRemoteMediator(
    private val networkClient: RecipeNetworkClient,
    private val recipeDao: RecipeDao,
    private val remoteKeyDao: RecipeRemoteKeyDao,
    private val tagsDao: TagDao,
    private val recipeTagDao: RecipeTagDao,
) : RemoteMediator<Int, RecipeSummary>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RecipeSummary>
    ): MediatorResult {
        val cursor: String? = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val key = remoteKeyDao.getKey()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                key.nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return try {
            val response = networkClient.getRecipes(next = cursor, requireTags = true)
                .fold(
                    ifLeft = { return MediatorResult.Error(Exception("Network error")) },
                    ifRight = { it }
                )

            if (loadType == LoadType.REFRESH) {
                recipeDao.clearAll()
                remoteKeyDao.clearKey()
            }

            recipeDao.upsertAll(response.items.map { it.toEntity() })
            tagsDao.upsertAll(response.items.flatMap { recipe -> recipe.tags.map { it.toEntity() } })
            recipeTagDao.insertAll(response.items.flatMap { recipe ->
                recipe.tags.map { RecipeTagCrossRef(recipeId = recipe.id, tagId = it.id) }
            })
            remoteKeyDao.upsert(
                RecipeRemoteKey(
                    id = "RECIPE_LIST",
                    nextKey = response.next,
                    createdAt = Clock.System.now()
                )
            )

            MediatorResult.Success(endOfPaginationReached = response.next == null)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
