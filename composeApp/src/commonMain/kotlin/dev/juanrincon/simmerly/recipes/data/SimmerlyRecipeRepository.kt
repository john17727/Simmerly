package dev.juanrincon.simmerly.recipes.data

import app.tracktion.core.domain.util.Result
import app.tracktion.core.domain.util.asEmptyDataResult
import app.tracktion.core.domain.util.fold
import app.tracktion.core.domain.util.mapError
import app.tracktion.core.domain.util.onSuccess
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
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
import dev.juanrincon.simmerly.recipes.domain.model.PaginationData
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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


    override fun recipeList(
        page: Int,
        perPage: Int,
        refresh: Boolean
    ): Flow<Result<LoadingResult<RecipeListResult>, RecipesError>> = flow {
        // Start with loading
        emit(Result.Success(LoadingResult.Loading))

        // Optionally clear for a hard refresh
        if (refresh) {
            recipeDao.clearAll()
        }

        var pagination: PaginationData? = null

        // Fetch from network and persist
        val networkResult = networkClient.getRecipes(page, perPage, true)
        networkResult.fold(
            onSuccess = { response ->
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
                pagination = response.toPaginationData()
            },
            onFailure = {
                // Surface fetch error but continue to emit cached/updated DB data
                emit(Result.Error(RecipesError.FetchError))
            }
        )

        // Now emit the DB-backed list
        val dbFlow = sessionDataStore.observeServerAddress()
            .combine(recipeDao.observeRecipeList()) { address, recipes ->
                recipes.map { it.toDomain(address) }
            }
            .map { list ->
                Result.Success(LoadingResult.Loaded(RecipeListResult(list, pagination)))
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

    override fun recipeDetails(id: String): Flow<Result<LoadingResult<RecipeDetail>, RecipesError>> =
        flow {
            // Notify observers we're loading fresh data
            emit(Result.Success(LoadingResult.Loading))

            // Try to refresh from network and persist to local DB first
            val networkResult = networkClient.getRecipe(id)
            networkResult.fold(
                onSuccess = { dto ->
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
                },
                onFailure = {
                    // Surface fetch error but still proceed to emit cached DB flow next
                    emit(Result.Error(RecipesError.FetchError))
                }
            )

            // Now emit the database-backed flow (latest if refresh succeeded)
            val dbFlow = sessionDataStore.observeServerAddress()
                .combine(recipeDao.observeRecipeDetail(id)) { address, entity ->
                    entity.toDomain(address)
                }
                .map { Result.Success(LoadingResult.Loaded(it)) }
                .distinctUntilChanged()

            emitAll(dbFlow)
        }

    override suspend fun addComment(
        recipeId: String,
        text: String
    ): Result<Unit, RecipesError> =
        networkClient.addComment(recipeId, text).onSuccess { comment ->
            commentDao.upsert(comment.toEntity())
        }.mapError { RecipesError.UpdateError }.asEmptyDataResult()

    override suspend fun updateSettings(
        recipeId: String,
        settings: Settings
    ): Result<Unit, RecipesError> =
        networkClient.patchRecipe(recipeId, RecipePatchDto(settings = settings.toDto())).onSuccess {
            recipeDao.upsert(it.toEntity())
        }.mapError { RecipesError.UpdateError }.asEmptyDataResult()
}