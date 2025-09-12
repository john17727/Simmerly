package dev.juanrincon.simmerly.recipes.data.di

import dev.juanrincon.simmerly.core.di.AuthClient
import dev.juanrincon.simmerly.recipes.data.SimmerlyRecipeRepository
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.data.store.RecipeStore
import dev.juanrincon.simmerly.recipes.data.store.RecipeStoreFactory
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import org.koin.dsl.module
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi

@OptIn(ExperimentalStoreApi::class)
val recipeDataModule = module {
    single<RecipeNetworkClient> { RecipeNetworkClient(get(qualifier = AuthClient)) }
    single<RecipeStore> { RecipeStoreFactory(get(), get(), get()).create() }
    single<RecipeRepository> { SimmerlyRecipeRepository(get(), get(), get()) }
}