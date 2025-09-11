@file:OptIn(ExperimentalStoreApi::class)

package dev.juanrincon.simmerly.recipes.data.store

import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
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
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult


typealias RecipeStore = MutableStore<String, RecipeDetail>

class PostStoreFactory(
    private val client: RecipeNetworkClient,
    private val recipeDao: RecipeDao,
    private val sessionDataStore: SessionDataStore
) {

    fun create(): RecipeStore = MutableStoreBuilder.from(
        fetcher = createFetcher(),
        sourceOfTruth = createSourceOfTruth(),
        converter = createConverter()
    ).build(updater = createUpdater())

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
                recipeDao.upsert(response.recipe)
            }
        )

    private fun createConverter(): Converter<RecipeDetailDto, RecipeDetailWithRelations, RecipeDetail> =
        Converter.Builder<RecipeDetailDto, RecipeDetailWithRelations, RecipeDetail>()
            .fromNetworkToLocal { recipeDto ->
                recipeDto.toEntityWithRelations()
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