@file:OptIn(ExperimentalStoreApi::class)

package dev.juanrincon.simmerly.recipes.data.store

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.recipes.data.local.recipe.IngredientDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeToolCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.RecipeDetailWithRelations
import dev.juanrincon.simmerly.recipes.data.mappers.toDomain
import dev.juanrincon.simmerly.recipes.data.mappers.toEntityWithRelations
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeDetailDto
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import kotlinx.coroutines.flow.combine
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult


typealias RecipeStore = Store<String, RecipeDetail>

class RecipeStoreFactory(
    private val client: RecipeNetworkClient,
    private val database: SimmerlyDatabase,
    private val sessionDataStore: SessionDataStore
) {

    private val recipeDao = database.recipeDao()
    private val ingredientDao = database.ingredientDao()
    private val instructionsDao = database.instructionDao()
    private val toolsDao = database.toolDao()
    private val recipeToolDao = database.recipeToolDao()

    fun create(): RecipeStore = StoreBuilder.from(
        fetcher = createFetcher(),
        sourceOfTruth = createSourceOfTruth(),
        converter = createConverter()
    ).build()

    private fun createFetcher(): Fetcher<String, RecipeDetailDto> =
        Fetcher.ofResult { recipeId ->
            client.getRecipe(recipeId)
        }

    private fun createSourceOfTruth(): SourceOfTruth<String, RecipeDetailWithRelations, RecipeDetail> =
        SourceOfTruth.of(
            reader = { recipeId ->
                sessionDataStore.observeServerAddress()
                    .combine(recipeDao.observeRecipeDetail(recipeId)) { address, entity ->
                        entity.toDomain(address)
                    }
            },
            writer = { recipeId, response ->
                database.useWriterConnection { writerTransaction ->
                    writerTransaction.immediateTransaction {
                        recipeDao.upsert(response.recipe)
                        val units = response.ingredients.mapNotNull { it.unit }
                        if (units.isNotEmpty()) database.unitDao().upsertAll(units)

                        val foods = response.ingredients.mapNotNull { it.food }
                        if (foods.isNotEmpty()) database.foodDao().upsertAll(foods)

                        ingredientDao.upsertAll(response.ingredients.map { it.ingredient })
                        instructionsDao.upsertAll(response.instructions.map { it.instruction })
                        toolsDao.upsertAll(response.tools)
                        // Refresh cross‑refs for this recipe
                        val recipeId = response.recipe.id
                        val toolRefs = response.tools.map { tool ->
                            RecipeToolCrossRef(
                                recipeId = recipeId,
                                toolId = tool.id
                            )
                        }
                        recipeToolDao.clearForRecipe(recipeId)
                        recipeToolDao.insertAll(toolRefs)
                    }
                }
            }
        )

    private fun createConverter(): Converter<RecipeDetailDto, RecipeDetailWithRelations, RecipeDetail> =
        Converter.Builder<RecipeDetailDto, RecipeDetailWithRelations, RecipeDetail>()
            .fromNetworkToLocal { recipeDto ->
                recipeDto.toEntityWithRelations()
            }
            .fromOutputToLocal { recipeDetail ->
                recipeDetail.toEntityWithRelations()
            }
            .build()

    private fun createUpdater(): Updater<String, RecipeDetail, Boolean> = Updater.by(
        post = { recipeId, _ ->
            UpdaterResult.Success.Untyped("")
        }
    )

    private fun createBookkeeper(): Bookkeeper<String> {
        TODO()
    }
}