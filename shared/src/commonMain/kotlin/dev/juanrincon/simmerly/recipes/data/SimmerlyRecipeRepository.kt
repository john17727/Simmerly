package dev.juanrincon.simmerly.recipes.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import arrow.core.Either
import arrow.core.raise.either
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.recipes.data.local.recent.RecentSearchQueryEntity
import dev.juanrincon.simmerly.recipes.data.local.recent.RecentlyViewedEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeToolCrossRef
import dev.juanrincon.simmerly.recipes.data.mappers.toDomain
import dev.juanrincon.simmerly.recipes.data.mappers.toDto
import dev.juanrincon.simmerly.recipes.data.mappers.toEntity
import dev.juanrincon.simmerly.recipes.data.mappers.toEntityWithRelations
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeDetailDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing.RecipePatchDto
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.domain.model.Comment
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Clock

class SimmerlyRecipeRepository(
    private val networkClient: RecipeNetworkClient,
    private val database: SimmerlyDatabase,
    private val sessionDataStore: SessionDataStore,
) : RecipeRepository {
    private val recipeDao = database.recipeDao()
    private val ingredientDao = database.ingredientDao()
    private val instructionsDao = database.instructionDao()
    private val toolsDao = database.toolDao()
    private val tagsDao = database.tagDao()
    private val recipeToolDao = database.recipeToolDao()
    private val recipeTagDao = database.recipeTagDao()
    private val noteDao = database.noteDao()
    private val commentDao = database.commentDao()
    private val userDao = database.userDao()
    private val recentlyViewedDao = database.recentlyViewedDao()
    private val recentSearchQueryDao = database.recentSearchQueryDao()
    private val remoteKeyDao = database.recipeRemoteKeyDao()

    @OptIn(ExperimentalPagingApi::class)
    override fun recipeList(): Flow<PagingData<RecipeSummary>> = Pager(
        config = PagingConfig(pageSize = 50, enablePlaceholders = false),
        remoteMediator = RecipeRemoteMediator(
            networkClient = networkClient,
            recipeDao = recipeDao,
            remoteKeyDao = remoteKeyDao,
            tagsDao = tagsDao,
            recipeTagDao = recipeTagDao,
        ),
        pagingSourceFactory = {
            RecipePagingSource(
                recipeDao = recipeDao,
                preferenceDao = database.userRecipePreferenceDao(),
                sessionDataStore = sessionDataStore,
            )
        }
    ).flow

    override fun observeAllRecipes(): Flow<List<RecipeSummary>> {
        val preferenceDao = database.userRecipePreferenceDao()
        return combine(
            sessionDataStore.observeServerAddress(),
            recipeDao.observeRecipeList(),
            preferenceDao.observeAll(),
        ) { address, recipes, preferences ->
            val preferenceMap = preferences.associateBy { it.recipeId }
            recipes.map {
                it.toDomain(
                    address,
                    isFavorite = preferenceMap[it.recipe.id]?.isFavorite ?: false
                )
            }
        }.distinctUntilChanged()
    }

    override fun comments(recipeId: String): Flow<List<Comment>> =
        sessionDataStore.observeServerAddress()
            .combine(commentDao.observeComments(recipeId)) { address, comments ->
                comments.map { it.toDomain(address) }
            }

    override fun recipeDetails(id: String): Flow<Either<RecipesError, LoadingResult<RecipeDetail>>> =
        channelFlow {
            val hasCached = recipeDao.existsById(id) > 0

            if (!hasCached) {
                send(Either.Right(LoadingResult.Loading))
                val fetchResult = either {
                    val dto = networkClient.getRecipe(id).mapLeft { RecipesError.FetchError }.bind()
                    persistRecipeData(dto)
                }
                if (fetchResult.isLeft()) {
                    send(Either.Left((fetchResult as Either.Left).value))
                    return@channelFlow
                }
            } else {
                launch {
                    send(Either.Right(LoadingResult.Refreshing))
                    either {
                        val dto =
                            networkClient.getRecipe(id).mapLeft { RecipesError.FetchError }.bind()
                        persistRecipeData(dto)
                    }.onLeft { error ->
                        send(Either.Left(error))
                    }
                    send(Either.Right(LoadingResult.RefreshComplete))
                }
            }

            val preferenceDao = database.userRecipePreferenceDao()
            combine(
                sessionDataStore.observeServerAddress(),
                recipeDao.observeRecipeDetail(id),
                preferenceDao.observe(id),
            ) { address, entity, preference ->
                entity.toDomain(address, isFavorite = preference?.isFavorite ?: false)
            }
                .map { Either.Right(LoadingResult.Loaded(it)) }
                .distinctUntilChanged()
                .collect { send(it) }
        }

    private suspend fun persistRecipeData(dto: RecipeDetailDto) {
        val data = dto.toEntityWithRelations()
        val recipeId = data.recipe.id

        recipeDao.upsert(data.recipe)

        val units = data.ingredients.mapNotNull { it.unit }
        if (units.isNotEmpty()) database.unitDao().upsertAll(units)

        val foods = data.ingredients.mapNotNull { it.food }
        if (foods.isNotEmpty()) database.foodDao().upsertAll(foods)

        ingredientDao.deleteByRecipeId(recipeId)
        instructionsDao.deleteByRecipeId(recipeId)
        ingredientDao.upsertAll(data.ingredients.map { it.ingredient })
        instructionsDao.upsertAll(data.instructions.map { it.instruction })

        noteDao.deleteByRecipeId(recipeId)
        noteDao.upsertAll(data.notes)

        toolsDao.upsertAll(data.tools)

        userDao.upsertAll(data.comments.map { it.user })
        commentDao.deleteByRecipeId(recipeId)
        commentDao.upsertAll(data.comments.map { it.comment })

        val refs = data.tools.map { tool ->
            RecipeToolCrossRef(recipeId = recipeId, toolId = tool.id)
        }
        recipeToolDao.clearForRecipe(recipeId)
        recipeToolDao.insertAll(refs)
    }

    override suspend fun addComment(
        recipeId: String,
        text: String
    ): Either<RecipesError, Unit> = either {
        val comment =
            networkClient.addComment(recipeId, text).mapLeft { RecipesError.UpdateError }.bind()
        commentDao.upsert(comment.toEntity())
    }

    override suspend fun updateSettings(
        recipeId: String,
        settings: Settings
    ): Either<RecipesError, Unit> = either {
        val updatedSettings =
            networkClient.patchRecipe(recipeId, RecipePatchDto(settings = settings.toDto()))
                .mapLeft { RecipesError.UpdateError }.bind()
        recipeDao.upsert(updatedSettings.toEntity())
    }

    override fun observeRecentlyViewed(): Flow<List<RecipeSummary>> =
        sessionDataStore.observeServerAddress()
            .combine(recentlyViewedDao.observeWithRecipes()) { address, recipes ->
                recipes.map { it.toDomain(address) }
            }

    override suspend fun recordRecipeView(recipeId: String) {
        recentlyViewedDao.upsert(
            RecentlyViewedEntity(
                recipeId,
                Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override fun observeRecentSearchQueries(): Flow<List<String>> =
        recentSearchQueryDao.observe().map { list -> list.map { it.query } }

    override suspend fun recordSearchQuery(query: String) {
        recentSearchQueryDao.upsert(
            RecentSearchQueryEntity(
                query,
                Clock.System.now().toEpochMilliseconds()
            )
        )
    }
}
