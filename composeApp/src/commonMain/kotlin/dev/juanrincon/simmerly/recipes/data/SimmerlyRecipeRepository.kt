package dev.juanrincon.simmerly.recipes.data

import arrow.core.Either
import arrow.core.raise.either
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.recipes.data.local.recent.RecentSearchQueryEntity
import dev.juanrincon.simmerly.recipes.data.local.recent.RecentlyViewedEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeTagCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeToolCrossRef
import dev.juanrincon.simmerly.recipes.data.mappers.toDomain
import dev.juanrincon.simmerly.recipes.data.mappers.toDto
import dev.juanrincon.simmerly.recipes.data.mappers.toEntity
import dev.juanrincon.simmerly.recipes.data.mappers.toEntityWithRelations
import dev.juanrincon.simmerly.recipes.data.mappers.toPaginationData
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing.RecipePatchDto
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeListResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.domain.model.Comment
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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


    override fun recipeList(
        page: Int,
        perPage: Int,
        refresh: Boolean
    ): Flow<Either<RecipesError, LoadingResult<RecipeListResult>>> = flow {
        // Start with loading
        emit(Either.Right(LoadingResult.Loading))

        // Optionally clear for a hard refresh
        if (refresh) {
            recipeDao.clearAll()
        }

        // Fetch from network and persist
        val pagination = either {
            val response = networkClient.getRecipes(page, perPage, true).mapLeft {
                // Surface fetch error but continue to emit cached/updated DB data
                RecipesError.FetchError
            }.bind()

            recipeDao.upsertAll(response.items.map { it.toEntity() })
            tagsDao.upsertAll(response.items.flatMap { it.tags.map { tag -> tag.toEntity() } })
            val tagRefs = response.items.flatMap { recipe ->
                recipe.tags.map { tag ->
                    RecipeTagCrossRef(
                        recipeId = recipe.id,
                        tagId = tag.id
                    )
                }
            }
            recipeTagDao.insertAll(tagRefs)
            response.toPaginationData()
        }.fold(
            ifLeft = { emit(Either.Left(it)); null },   // surface error, keep going
            ifRight = { it }
        )

        // Now emit the DB-backed list
        val preferenceDao = database.userRecipePreferenceDao()
        val dbFlow = combine(
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
        }
            .map { list ->
                Either.Right(LoadingResult.Loaded(RecipeListResult(list, pagination)))
            }
            .distinctUntilChanged()

        emitAll(dbFlow)
    }

    override fun comments(recipeId: String): Flow<List<Comment>> =
        sessionDataStore.observeServerAddress()
            .combine(commentDao.observeComments(recipeId)) { address, comments ->
                comments.map { it.toDomain(address) }
            }

    // removed: loadRecipes(page, perPage, refresh) in favor of unified recipeList()

    override fun recipeDetails(id: String): Flow<Either<RecipesError, LoadingResult<RecipeDetail>>> =
        flow {
            // Notify observers we're loading fresh data
            emit(Either.Right(LoadingResult.Loading))

            // Try to refresh from network and persist to local DB first
            either {
                val dto = networkClient.getRecipe(id).mapLeft {
                    // Surface fetch error but still proceed to emit cached DB flow next
                    RecipesError.FetchError
                }.bind()
                val data = dto.toEntityWithRelations()
                val recipeId = data.recipe.id

                // Persist the latest details
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

                // Refresh cross refs for tools
                val refs = data.tools.map { tool ->
                    RecipeToolCrossRef(recipeId = recipeId, toolId = tool.id)
                }
                recipeToolDao.clearForRecipe(recipeId)
                recipeToolDao.insertAll(refs)
            }.fold(
                ifLeft = {
                    emit(Either.Left(it))
                },
                ifRight = {}
            )

            // Now emit the database-backed flow (latest if refresh succeeded)
            val preferenceDao = database.userRecipePreferenceDao()
            val dbFlow = combine(
                sessionDataStore.observeServerAddress(),
                recipeDao.observeRecipeDetail(id),
                preferenceDao.observe(id),
            ) { address, entity, preference ->
                entity.toDomain(address, isFavorite = preference?.isFavorite ?: false)
            }
                .map { Either.Right(LoadingResult.Loaded(it)) }
                .distinctUntilChanged()

            emitAll(dbFlow)
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